package com.juanbenevento.wms.audit.infrastructure.listener;

import com.juanbenevento.wms.inventory.domain.event.StockReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AnalyticsListener {

    @EventListener
    public void registerMetric(StockReceivedEvent event) {
        log.info("📊 [ANALYTICS] Registrando métrica: INBOUND_FLOW +1 en ubicación {}", event.locationCode());
    }
}