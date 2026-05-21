package com.vex.queries.jpa.id;

public class SnowflakeIdWorker {
    private final long workerId;
    private final long dataCenterId;
    private long sequence = 0L;
    private final long twepoch = 1735689600000L;
    private final long workerIdBits = 5L;
    private final long dataCenterIdBits = 5L;
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long maxDataCenterId = ~(-1L << dataCenterIdBits);
    private final long sequenceBits = 12L;
    private final long workerIdShift = sequenceBits;
    private final long dataCenterIdShift = sequenceBits + workerIdBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits + dataCenterIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);
    private long lastTimestamp = -1L;

    public SnowflakeIdWorker(long workerId, long dataCenterId) {
        if (workerId > maxWorkerId || workerId < 0) throw new IllegalArgumentException();
        if (dataCenterId > maxDataCenterId || dataCenterId < 0) throw new IllegalArgumentException();
        this.workerId = workerId;
        this.dataCenterId = dataCenterId;
    }

    public synchronized long nextId() {
        long timestamp = timeGen();
        if (timestamp < lastTimestamp) throw new RuntimeException("时钟回拨");
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) timestamp = tilNextMillis(lastTimestamp);
        } else sequence = 0;
        lastTimestamp = timestamp;
        return ((timestamp - twepoch) << timestampLeftShift) | (dataCenterId << dataCenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) timestamp = timeGen();
        return timestamp;
    }

    private long timeGen() {
        return System.currentTimeMillis();
    }
}