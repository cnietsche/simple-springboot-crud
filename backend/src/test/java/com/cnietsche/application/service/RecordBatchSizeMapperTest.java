package com.cnietsche.application.service;

import com.cnietsche.domain.exception.InvalidRecordBatchSizeException;
import com.cnietsche.domain.model.RecordBatchSize;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RecordBatchSizeMapperTest {

    @Test
    void shouldMapValidCounts() {
        assertThat(RecordBatchSizeMapper.fromCount(50)).isEqualTo(RecordBatchSize.SIZE_50);
        assertThat(RecordBatchSizeMapper.fromCount(1000)).isEqualTo(RecordBatchSize.SIZE_1000);
    }

    @Test
    void shouldRejectInvalidCount() {
        assertThatThrownBy(() -> RecordBatchSizeMapper.fromCount(25))
                .isInstanceOf(InvalidRecordBatchSizeException.class);
    }
}
