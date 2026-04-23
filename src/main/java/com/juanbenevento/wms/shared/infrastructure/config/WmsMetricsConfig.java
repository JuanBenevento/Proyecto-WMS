package com.juanbenevento.wms.shared.infrastructure.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Custom metrics configuration for WMS observability.
 * 
 * Metrics exposed:
 * - wms.orders.created (counter)
 * - wms.orders.confirmed (counter)
 * - wms.orders.cancelled (counter)
 * - wms.inventory.received (counter)
 * - wms.inventory.moved (counter)
 * - wms.picking.duration (timer)
 * - wms.tenants.active (gauge)
 */
@Configuration
public class WmsMetricsConfig {

    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

    @Bean
    public WmsMetrics wmsMetrics(MeterRegistry registry) {
        return new WmsMetrics(registry);
    }

    /**
     * WMS Metrics facade for easy metric recording.
     */
    public static class WmsMetrics {

        private final MeterRegistry registry;
        private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>();

        public WmsMetrics(MeterRegistry registry) {
            this.registry = registry;
        }

        /**
         * Increment a counter by name.
         */
        public void incrementCounter(String name) {
            counters.computeIfAbsent(name, k ->
                Counter.builder("wms." + name)
                    .description("WMS event counter: " + name)
                    .tag("type", name)
                    .register(registry)
            ).increment();
        }

        /**
         * Increment a counter by name and tags.
         */
        public void incrementCounter(String name, String tagKey, String tagValue) {
            String key = name + "." + tagKey + "=" + tagValue;
            counters.computeIfAbsent(key, k ->
                Counter.builder("wms." + name)
                    .description("WMS event counter: " + name)
                    .tag(tagKey, tagValue)
                    .register(registry)
            ).increment();
        }

        /**
         * Record a timing.
         */
        public Timer.Sample startTimer() {
            return Timer.start(registry);
        }

        /**
         * Stop timer and record duration.
         */
        public void stopTimer(String name, Timer.Sample sample) {
            timers.computeIfAbsent(name, k ->
                Timer.builder("wms." + name)
                    .description("WMS operation timer: " + name)
                    .tag("operation", name)
                    .register(registry)
            ).stop(sample);
        }

        /**
         * Increment order created counter.
         */
        public void orderCreated() {
            incrementCounter("orders.created");
        }

        /**
         * Increment order confirmed counter.
         */
        public void orderConfirmed() {
            incrementCounter("orders.confirmed");
        }

        /**
         * Increment order cancelled counter.
         */
        public void orderCancelled() {
            incrementCounter("orders.cancelled");
        }

        /**
         * Increment inventory received counter.
         */
        public void inventoryReceived() {
            incrementCounter("inventory.received");
        }

        /**
         * Increment inventory moved counter.
         */
        public void inventoryMoved() {
            incrementCounter("inventory.moved");
        }

        /**
         * Record picking duration.
         */
        public void pickingCompleted(Timer.Sample sample) {
            stopTimer("picking.duration", sample);
        }
    }
}