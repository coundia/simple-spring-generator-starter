package com.pcoundia.config.manager;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// Enable jpa listener for auto insert or auto update createdAt and createdBy
@EnableJpaAuditing
@Configuration
public class PersistenceConfig {

    @Bean
    AuditorAware<Long> auditorProvider() {
        return new AuditorAwareImpl();
    }
}
