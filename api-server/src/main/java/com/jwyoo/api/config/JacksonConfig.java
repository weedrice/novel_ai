package com.jwyoo.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        Hibernate6Module hibernate6Module = new Hibernate6Module();
        // Force lazy loading proxy initialization
        hibernate6Module.disable(Hibernate6Module.Feature.USE_TRANSIENT_ANNOTATION);
        hibernate6Module.enable(Hibernate6Module.Feature.FORCE_LAZY_LOADING);

        return Jackson2ObjectMapperBuilder
                .json()
                .modulesToInstall(hibernate6Module)
                .build();
    }
}