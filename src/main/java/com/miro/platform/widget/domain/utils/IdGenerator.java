package com.miro.platform.widget.domain.utils;

import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
public class IdGenerator {

    AtomicLong globalId;

    public IdGenerator() {
        this.globalId = new AtomicLong(0L);
    }

    public IdGenerator(long startFrom) {
        this.globalId = new AtomicLong(startFrom);
    }

    public long getNextId() {
        return globalId.getAndAdd(1L);
    }
}
