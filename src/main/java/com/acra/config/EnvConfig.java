package com.acra.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class EnvConfig {
    static final Logger logger = LoggerFactory.getLogger(EnvConfig.class);
    @Bean
    public Dotenv dotenv() {
        return Dotenv.load();
    }

    @Bean
    public MapPropertySource dotenvPropertySource(Dotenv dotenv, ConfigurableEnvironment env) {
        Map<String, Object> properties = new HashMap<>();
        for (DotenvEntry entry : dotenv.entries()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        MapPropertySource propertySource = new MapPropertySource("dotenvProperties", properties);
        env.getPropertySources().addFirst(propertySource);
        logger.info("properties:", env);
        return propertySource;
    }
}