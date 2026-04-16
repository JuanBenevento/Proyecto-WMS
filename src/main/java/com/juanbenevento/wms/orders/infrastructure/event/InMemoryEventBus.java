package com.juanbenevento.wms.orders.infrastructure.event;

import com.juanbenevento.wms.orders.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementación in-memory del EventBus.
 * 
 * Características:
 * - Procesamiento asíncrono con executor pool
 * - Retry exponencial con backoff
 * - Dead Letter Queue para eventos fallidos
 * - Thread-safe
 * 
 * PREPARADO para swap por KafkaEventBus o RabbitMQEventBus
 * sin cambiar código de dominio.
 */
@Component
public class InMemoryEventBus implements EventBus {
    
    private static final Logger log = LoggerFactory.getLogger(InMemoryEventBus.class);
    
    // Configuración de retry
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 1000; // 1 segundo
    
    // Handlers suscritos por tipo de evento
    private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> handlers = 
        new ConcurrentHashMap<>();
    
    // Cola de eventos pendientes
    private final ConcurrentLinkedQueue<PendingEventWrapper> pendingEvents = new ConcurrentLinkedQueue<>();
    
    // Cola de Dead Letter
    private final ConcurrentLinkedQueue<DeadLetterEvent> deadLetterQueue = new ConcurrentLinkedQueue<>();
    
    // Contadores para métricas
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);
    
    // Executor para procesamiento asíncrono
    private final ExecutorService executor;
    
    public InMemoryEventBus() {
        // Pool de threads para procesamiento de eventos
        this.executor = Executors.newFixedThreadPool(
            4, 
            r -> {
                Thread t = new Thread(r, "event-bus-processor");
                t.setDaemon(true);
                return t;
            }
        );
        
        // Scheduler para retry con exponential backoff
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        scheduler.scheduleAtFixedRate(
            this::processPendingEvents, 
            100, // delay inicial
            100, // intervalo
            TimeUnit.MILLISECONDS
        );
        
        log.info("InMemoryEventBus inicializado con pool de 4 processors");
    }
    
    @Override
    public void publish(DomainEvent event) {
        log.debug("Publicando evento: {} [{}]", event.getEventType(), event.getEventId());
        
        // Encontrar handlers para este tipo de evento
        List<EventHandler<? extends DomainEvent>> eventHandlers = findHandlers(event.getClass());
        
        if (eventHandlers.isEmpty()) {
            log.debug("No hay handlers suscritos para {}", event.getEventType());
            return;
        }
        
        // Encolar para procesamiento asíncrono
        for (EventHandler<? extends DomainEvent> handler : eventHandlers) {
            pendingEvents.add(new PendingEventWrapper(event, handler, 0, System.currentTimeMillis()));
        }
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void subscribe(Class<T> eventClass, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
               .add(handler);
        log.info("Handler suscripto a eventos de tipo: {}", eventClass.getSimpleName());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends DomainEvent> void unsubscribe(Class<T> eventClass, EventHandler<T> handler) {
        List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventClass);
        if (eventHandlers != null) {
            eventHandlers.remove(handler);
            log.info("Handler desuscripto de tipo: {}", eventClass.getSimpleName());
        }
    }
    
    @Override
    public int getPendingEventCount() {
        return pendingEvents.size();
    }
    
    @Override
    public int getDeadLetterCount() {
        return deadLetterQueue.size();
    }
    
    /**
     * Obtiene todos los eventos en la Dead Letter Queue.
     */
    public List<DeadLetterEvent> getDeadLetterEvents() {
        return new ArrayList<>(deadLetterQueue);
    }
    
    /**
     * Reintenta un evento específico desde la DLQ.
     */
    @SuppressWarnings("unchecked")
    public void retryFromDeadLetter(DeadLetterEvent dlqEvent) {
        log.info("Reintentando evento desde DLQ: {}", dlqEvent.event.getEventId());
        EventHandler<DomainEvent> handler = (EventHandler<DomainEvent>) dlqEvent.originalHandler;
        pendingEvents.add(new PendingEventWrapper(
            dlqEvent.event, 
            handler, 
            0,  // reset retry count
            System.currentTimeMillis()
        ));
        deadLetterQueue.remove(dlqEvent);
    }
    
    /**
     * Procesa eventos pendientes con retry y DLQ.
     */
    private void processPendingEvents() {
        PendingEventWrapper wrapper;
        while ((wrapper = pendingEvents.poll()) != null) {
            final PendingEventWrapper eventWrapper = wrapper;
            executor.submit(() -> processEvent(eventWrapper));
        }
    }
    
    @SuppressWarnings("unchecked")
    private <T extends DomainEvent> void processEvent(PendingEventWrapper wrapper) {
        try {
            EventHandler<T> handler = (EventHandler<T>) wrapper.handler;
            handler.handle((T) wrapper.event);
            processedCount.incrementAndGet();
            log.debug("Evento procesado exitosamente: {}", wrapper.event.getEventId());
            
        } catch (Exception e) {
            handleFailure(wrapper, e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void handleFailure(PendingEventWrapper wrapper, Exception e) {
        int currentRetry = wrapper.retryCount;
        
        if (currentRetry < MAX_RETRIES) {
            // Calcular delay con exponential backoff
            long backoffMs = INITIAL_BACKOFF_MS * (long) Math.pow(2, currentRetry);
            long nextRetryTime = wrapper.enqueuedAt + backoffMs;
            
            if (System.currentTimeMillis() >= nextRetryTime) {
                // Re-encolar para retry
                log.warn("Retry {} de {} para evento {}: {}", 
                    currentRetry + 1, MAX_RETRIES, wrapper.event.getEventId(), e.getMessage());
                
                pendingEvents.add(new PendingEventWrapper(
                    wrapper.event, 
                    wrapper.handler, 
                    currentRetry + 1, 
                    nextRetryTime
                ));
            } else {
                // Aún no es tiempo de retry, volver a la cola
                pendingEvents.add(wrapper);
            }
        } else {
            // Max retries alcanzado → DLQ
            log.error("Evento movido a DLQ después de {} intentos: {}", 
                MAX_RETRIES, wrapper.event.getEventId(), e);
            
            deadLetterQueue.add(new DeadLetterEvent(
                wrapper.event,
                wrapper.handler,
                currentRetry,
                e.getMessage(),
                System.currentTimeMillis()
            ));
            
            failedCount.incrementAndGet();
        }
    }
    
    /**
     * Encuentra todos los handlers que matchean con el tipo del evento.
     * Incluye handlers de clases padre/interfaces.
     */
    @SuppressWarnings("unchecked")
    private List<EventHandler<? extends DomainEvent>> findHandlers(Class<? extends DomainEvent> eventClass) {
        List<EventHandler<? extends DomainEvent>> result = new ArrayList<>();
        
        // Buscar handlers exactos
        List<EventHandler<? extends DomainEvent>> exact = handlers.get(eventClass);
        if (exact != null) {
            result.addAll(exact);
        }
        
        // Buscar handlers de superclases e interfaces
        for (Class<?> superClass = eventClass.getSuperclass(); 
             superClass != null && DomainEvent.class.isAssignableFrom(superClass);
             superClass = superClass.getSuperclass()) {
            List<EventHandler<? extends DomainEvent>> parent = handlers.get(superClass);
            if (parent != null) {
                for (EventHandler<? extends DomainEvent> h : parent) {
                    if (!result.contains(h)) {
                        result.add(h);
                    }
                }
            }
        }
        
        return result;
    }
    
    // --- Wrapper Classes ---
    
    private record PendingEventWrapper(
        DomainEvent event,
        EventHandler<? extends DomainEvent> handler,
        int retryCount,
        long enqueuedAt
    ) {}
    
    public record DeadLetterEvent(
        DomainEvent event,
        EventHandler<? extends DomainEvent> originalHandler,
        int retryCount,
        String lastError,
        long failedAt
    ) {}
    
    // --- Métricas ---
    
    public int getProcessedCount() {
        return processedCount.get();
    }
    
    public int getFailedCount() {
        return failedCount.get();
    }
    
    public void shutdown() {
        executor.shutdown();
    }
}
