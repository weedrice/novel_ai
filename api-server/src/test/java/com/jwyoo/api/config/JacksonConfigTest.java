package com.jwyoo.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigTest {

    @Test
    @DisplayName("ObjectMapper Bean 생성 테스트")
    void objectMapper_BeanCreated() {
        // given
        JacksonConfig jacksonConfig = new JacksonConfig();

        // when
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        // then
        assertThat(objectMapper).isNotNull();
    }

    @Test
    @DisplayName("ObjectMapper에 Hibernate6Module 등록 확인")
    void objectMapper_Hibernate6ModuleRegistered() {
        // given
        JacksonConfig jacksonConfig = new JacksonConfig();

        // when
        ObjectMapper objectMapper = jacksonConfig.objectMapper();

        // then
        assertThat(objectMapper).isNotNull();
        // Hibernate6Module이 등록되었는지 간접적으로 확인
        assertThat(objectMapper.getRegisteredModuleIds()).isNotEmpty();
    }
}
