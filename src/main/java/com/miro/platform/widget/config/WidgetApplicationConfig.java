package com.miro.platform.widget.config;

import com.miro.platform.widget.domain.repository.WidgetRepo;
import com.miro.platform.widget.domain.service.WidgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.locks.StampedLock;

@Configuration
public class WidgetApplicationConfig {
    @Autowired
    private ApplicationContext context;

    @Bean
    @Primary
    public WidgetService WidgetService(@Value("${service.type}") String qualifier) {
        return (WidgetService) context.getBean(qualifier);
    }

    @Bean
    @Primary
    public WidgetRepo WidgetRepo(@Value("${storage.type}") String qualifier) {
        return (WidgetRepo) context.getBean(qualifier);
    }

    @Bean
    public StampedLock StampedLock() {
        return new StampedLock();
    }
}
