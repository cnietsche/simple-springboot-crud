package com.cnietsche.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RandomOverloadValueGeneratorTest {

    @Test
    void shouldGenerateValueWithinLimit() {
        String value = RandomOverloadValueGenerator.generate();
        assertThat(value).isNotBlank();
        assertThat(value.length()).isLessThanOrEqualTo(255);
        assertThat(value).doesNotContain("\n");
    }
}
