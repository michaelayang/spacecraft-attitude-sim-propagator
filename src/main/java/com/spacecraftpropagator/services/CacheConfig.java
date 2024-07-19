package com.spacecraftpropagator.services;

import java.util.Arrays;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public CacheManager cacheManager() {
    ConcurrentMapCacheManager mgr = new ConcurrentMapCacheManager();
    mgr.setCacheNames(Arrays.asList("keplerianRecords", "keplerianRecord", "ptolemaicRecord", "truthDataRecord"));
    return mgr;
  }
}
