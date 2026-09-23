package com.taxapi;

import com.taxapi.service.StorageService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public StorageService storageService() {
        return new LocalStorageService();
    }
}
