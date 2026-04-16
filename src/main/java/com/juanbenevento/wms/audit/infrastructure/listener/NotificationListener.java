package com.juanbenevento.wms.audit.infrastructure.listener;

import com.juanbenevento.wms.inventory.domain.event.StockReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j // Lombok nos da un logger "log" gratis
public class NotificationListener {

    @Async // ¡Magia! Esto corre en un hilo separado para no frenar la respuesta al usuario
    @EventListener
    public void handleStockReceived(StockReceivedEvent event) {
        // Simulación de envío de correo
        log.info("📧 [EMAIL SERVICE] Enviando correo al Gerente: 'Llegó LPN {} con {} u. de {}'",
                event.lpn(), event.quantity(), event.sku());

        // Aquí iría la lógica real de JavaMailSender
        try {
            Thread.sleep(1000); // Simulamos demora de red
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Email notification interrupted, aborting");
            return;
        }
    }
}