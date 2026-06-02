package com.cnietsche.domain.model;

/**
 * Allowed batch sizes for bulk record generation (shared across features).
 */
public enum RecordBatchSize {
    SIZE_50(50),
    SIZE_100(100),
    SIZE_400(400),
    SIZE_1000(1000);

    private final int count;

    RecordBatchSize(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
