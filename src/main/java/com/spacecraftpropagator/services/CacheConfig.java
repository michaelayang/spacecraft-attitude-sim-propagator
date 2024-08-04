// Copyright (C) 2024, M. Yang 
// 
//     This program is free software: you can redistribute it and/or modify
//     it under the terms of the GNU General Public License as published by
//     the Free Software Foundation, either version 3 of the License, or
//     (at your option) any later version.
// 
//     This program is distributed in the hope that it will be useful, 
//     but WITHOUT ANY WARRANTY; without even the implied warranty of
//     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
//     GNU General Public License for more details.
// 
//     For further details, please see the README.md file included
//     with this software, and/or the GNU General Public License html
//     file which is also included with this software.

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
