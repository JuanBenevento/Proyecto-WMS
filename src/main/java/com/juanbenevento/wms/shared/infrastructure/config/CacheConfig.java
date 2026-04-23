package com.juanbenevento.wms.shared.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for WMS using Caffeine (in-memory cache).
 * 
 * For production with distributed caching, replace with Redis:
 * - spring-boot-starter-data-redis
 * - RedisCacheManager
 * 
 * Cache strategy:
 * - Products: cache by SKU, TTL 30min
 * - Locations: cache all, TTL 15min  
 * - Orders: cache by ID, TTL 5min
 * - Inventory: cache queries, TTL 2min
 */
@Configuration
@EnableCaching
@Slf4j
public class CacheConfig {

    @Bean
    @Primary
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        
        cacheManager.registerCustomCache("products", 
            Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("locations",
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(15, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("orders",
            Caffeine.newBuilder()
                .maximumSize(200)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("inventory",
            Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(2, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        cacheManager.registerCustomCache("tenants",
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(60, TimeUnit.MINUTES)
                .recordStats()
                .build());
        
        log.info("Caffeine cache manager initialized with 5 cache regions");
        return cacheManager;
    }
}