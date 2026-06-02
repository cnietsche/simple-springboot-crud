package com.cnietsche.application.service;

import com.cnietsche.domain.exception.InvalidRecordBatchSizeException;
import com.cnietsche.domain.model.RecordBatchSize;

public final class RecordBatchSizeMapper {

    private RecordBatchSizeMapper() {
    }

    public static RecordBatchSize fromCount(int count) {
        for (RecordBatchSize size : RecordBatchSize.values()) {
            if (size.getCount() == count) {
                return size;
            }
        }
        throw new InvalidRecordBatchSizeException();
    }
}
