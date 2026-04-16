package com.juanbenevento.wms.orders.infrastructure.event;

import com.juanbenevento.wms.orders.domain.event.DomainEvent;

/**
 * Interfaz para el bus de eventos de dominio.
 * 
 * Esta abstracción permite:
 * - Implementación in-memory para desarrollo/testing
 * - Swap por Kafka/RabbitMQ sin cambiar código de dominio
 * 
 * Los handlers se registran por tipo de evento y son ejecutados
 * de forma asíncrona con retry automático.
 */
public interface EventBus {
    
    /**
     * Publica un evento de dominio.
     * El evento será entregado a todos los handlers suscritos.
     */
    void publish(DomainEvent event);
    
    /**
     * Suscribe un handler a un tipo específico de evento.
     * El handler será llamado cuando se publique un evento del tipo especificado.
     * 
     * @param eventClass Tipo de evento al que suscribirse
     * @param handler Handler que procesará los eventos
     * @param <T> Tipo del evento
     */
    <T extends DomainEvent> void subscribe(Class<T> eventClass, EventHandler<T> handler);
    
    /**
     * Desuscribe un handler de un tipo de evento.
     */
    <T extends DomainEvent> void unsubscribe(Class<T> eventClass, EventHandler<T> handler);
    
    /**
     * Obtiene el número de eventos pendientes de procesar.
     */
    int getPendingEventCount();
    
    /**
     * Obtiene el número de eventos en DLQ (fallidos).
     */
    int getDeadLetterCount();
    
    /**
     * Interfaz que deben implementar los handlers de eventos.
     */
    @FunctionalInterface
    interface EventHandler<T extends DomainEvent> {
        /**
         * Procesa el evento.
         * 
         * @param event El evento a procesar
         * @throws Exception si hay un error procesando el evento
         */
        void handle(T event) throws Exception;
    }
}
