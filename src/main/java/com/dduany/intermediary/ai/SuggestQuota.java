package com.dduany.intermediary.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/** Caps POST /ai/suggest calls per UTC day. In memory: a restart resets the count. */
@Component
public class SuggestQuota {

    private final Clock clock;
    private final int dailyLimit;
    private LocalDate day;
    private int used;

    public SuggestQuota(Clock clock, @Value("${app.ai.daily-suggest-limit}") int dailyLimit) {
        this.clock = clock;
        this.dailyLimit = dailyLimit;
    }

    /** Counts one call; false when today's allowance is already spent. */
    public synchronized boolean tryAcquire() {
        LocalDate today = LocalDate.now(clock);
        if (!today.equals(day)) {
            day = today;
            used = 0;
        }
        if (used >= dailyLimit) return false;
        used++;
        return true;
    }
}
