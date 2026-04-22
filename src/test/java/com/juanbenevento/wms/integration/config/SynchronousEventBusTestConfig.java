package com.juanbenevento.wms.integration.config;

import com.juanbenevento.wms.orders.application.port.out.DomainEventRepositoryPort;
import com.juanbenevento.wms.orders.domain.event.DomainEvent;
import com.juanbenevento.wms.orders.infrastructure.event.EventBus;
import com.juanbenevento.wms.orders.infrastructure.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test configuration with SYNCHRONOUS EventBus for reliable integration testing.
 * 
 * Unlike the production InMemoryEventBus which processes events asynchronously,
 * this version processes events synchronously so that tests can verify event
 * persistence before the transaction commits.
 * 
 * Key differences from production:
 * - Events are processed synchronously (no @Async)
 * - No retry logic (fail fast for tests)
 * - No Dead Letter Queue
 */
@TestConfiguration
public class SynchronousEventBusTestConfig {

    private static final Logger log = LoggerFactory.getLogger(SynchronousEventBusTestConfig.class);

    @Bean
    @Primary
    public EventBus eventBus(DomainEventRepositoryPort eventRepository) {
        return new SynchronousEventBus(eventRepository);
    }

    /**
     * Synchronous implementation of EventBus for testing.
     * All events are processed immediately within the same thread.
     */
    public static class SynchronousEventBus implements EventBus {
        
        private static final Logger log = LoggerFactory.getLogger(SynchronousEventBus.class);
        
        private final DomainEventRepositoryPort eventRepository;
        private final Map<Class<? extends DomainEvent>, List<EventHandler<? extends DomainEvent>>> handlers = 
            new CopyOnWriteHashMap<>();
        
        private final AtomicInteger processedCount = new AtomicInteger(0);

        public SynchronousEventBus(DomainEventRepositoryPort eventRepository) {
            this.eventRepository = eventRepository;
            log.info("SynchronousEventBus inicializado para testing");
        }

        @Override
        public void publish(DomainEvent event) {
            log.debug("TEST: Publicando evento síncrono: {} [{}]", event.getEventType(), event.getEventId());
            
            // 1. Persistir evento (auditoría)
            try {
                eventRepository.save(event);
                log.debug("TEST: Evento persistido: {}", event.getEventId());
            } catch (Exception e) {
                log.error("TEST ERROR: No se pudo persistir evento {}: {}", event.getEventId(), e.getMessage());
                throw new RuntimeException("Failed to persist event in test", e);
            }
            
            // 2. Encontrar y ejecutar handlers SINCRONICAMENTE
            List<EventHandler<? extends DomainEvent>> eventHandlers = findHandlers(event.getClass());
            
            if (eventHandlers.isEmpty()) {
                log.debug("TEST: No hay handlers para {}", event.getEventType());
                return;
            }
            
            // 3. Procesar cada handler INMEDIATAMENTE
            for (EventHandler<? extends DomainEvent> handler : eventHandlers) {
                try {
                    @SuppressWarnings("unchecked")
                    EventHandler<DomainEvent> h = (EventHandler<DomainEvent>) handler;
                    h.handle(event);
                    processedCount.incrementAndGet();
                    log.debug("TEST: Handler procesado para evento: {}", event.getEventId());
                } catch (Exception e) {
                    log.error("TEST ERROR: Handler falló para evento {}: {}", event.getEventId(), e.getMessage());
                    throw new RuntimeException("Handler failed in test", e);
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends DomainEvent> void subscribe(Class<T> eventClass, EventHandler<T> handler) {
            handlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                   .add(handler);
            log.debug("TEST: Handler suscripto a: {}", eventClass.getSimpleName());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T extends DomainEvent> void unsubscribe(Class<T> eventClass, EventHandler<T> handler) {
            List<EventHandler<? extends DomainEvent>> eventHandlers = handlers.get(eventClass);
            if (eventHandlers != null) {
                eventHandlers.remove(handler);
            }
        }

        @Override
        public int getPendingEventCount() {
            return 0; // Synchronous - no pending events
        }

        @Override
        public int getDeadLetterCount() {
            return 0; // Synchronous - no DLQ
        }

        public int getProcessedCount() {
            return processedCount.get();
        }

        @SuppressWarnings("unchecked")
        private <T extends DomainEvent> List<EventHandler<? extends DomainEvent>> findHandlers(Class<T> eventClass) {
            List<EventHandler<? extends DomainEvent>> result = new ArrayList<>();
            
            // Buscar handlers exactos
            List<EventHandler<? extends DomainEvent>> exact = handlers.get(eventClass);
            if (exact != null) {
                result.addAll(exact);
            }
            
            // Buscar handlers de superclases
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
    }

    /**
     * Thread-safe Map implementation for handlers.
     */
    private static class CopyOnWriteHashMap<K, V> extends ConcurrentHashMap<K, V> {
        @Override
        public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
            return super.computeIfAbsent(key, mappingFunction);
        }
    }
}