package com.juanbenevento.wms.orders.infrastructure.event;

import com.juanbenevento.wms.orders.application.port.out.DomainEventRepositoryPort;
import com.juanbenevento.wms.orders.domain.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listener que persiste los eventos de dominio de Orders a la tabla domain_events.
 * 
 * Este listener captura todos los eventos de Orders (OrderCreatedEvent, OrderCancelledEvent, etc.)
 * y los persista para auditoría y trazabilidad.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventPersistenceListener {

    private final DomainEventRepositoryPort domainEventRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCreated(OrderCreatedEvent event) {
        log.debug("Persistiendo OrderCreatedEvent para orden {}", event.getAggregateId());
        domainEventRepository.save(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderCancelled(OrderCancelledEvent event) {
        log.debug("Persistiendo OrderCancelledEvent para orden {}", event.getAggregateId());
        domainEventRepository.save(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderShipped(OrderShippedEvent event) {
        log.debug("Persistiendo OrderShippedEvent para orden {}", event.getAggregateId());
        domainEventRepository.save(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {
        log.debug("Persistiendo OrderStatusChangedEvent para orden {}", event.getAggregateId());
        domainEventRepository.save(event);
    }
}