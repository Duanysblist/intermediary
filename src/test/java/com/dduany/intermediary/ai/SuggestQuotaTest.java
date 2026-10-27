package com.dduany.intermediary.ai;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class SuggestQuotaTest {

    static final class MovableClock extends Clock {
        Instant now = Instant.parse("2026-10-26T23:00:00Z");

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void allowsTheDailyLimitThenRefusesUntilTheUtcDayChanges() {
        MovableClock clock = new MovableClock();
        SuggestQuota quota = new SuggestQuota(clock, 2);

        assertThat(quota.tryAcquire()).isTrue();
        assertThat(quota.tryAcquire()).isTrue();
        assertThat(quota.tryAcquire()).isFalse();

        clock.now = Instant.parse("2026-10-27T00:00:00Z");
        assertThat(quota.tryAcquire()).isTrue();
    }
}
