package com.dduany.intermediary.auth;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class LoginThrottleTest {

    /** A clock the test can move forward. */
    static final class MovableClock extends Clock {
        Instant now = Instant.parse("2026-10-26T12:00:00Z");

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    private final MovableClock clock = new MovableClock();
    private final LoginThrottle throttle = new LoginThrottle(clock);

    @Test
    void blocksAfterFiveFailuresWithinTheWindow() {
        for (int i = 0; i < LoginThrottle.MAX_FAILURES - 1; i++) throttle.recordFailure();
        assertThat(throttle.isBlocked()).isFalse();
        throttle.recordFailure();
        assertThat(throttle.isBlocked()).isTrue();
    }

    @Test
    void unblocksWhenTheWindowPasses() {
        for (int i = 0; i < LoginThrottle.MAX_FAILURES; i++) throttle.recordFailure();
        clock.now = clock.now.plus(LoginThrottle.WINDOW);
        assertThat(throttle.isBlocked()).isTrue();
        clock.now = clock.now.plusSeconds(1);
        assertThat(throttle.isBlocked()).isFalse();
    }

    @Test
    void successfulLoginClearsTheCount() {
        for (int i = 0; i < LoginThrottle.MAX_FAILURES - 1; i++) throttle.recordFailure();
        throttle.reset();
        throttle.recordFailure();
        assertThat(throttle.isBlocked()).isFalse();
    }

    @Test
    void oldFailuresDoNotCount() {
        for (int i = 0; i < LoginThrottle.MAX_FAILURES - 1; i++) throttle.recordFailure();
        clock.now = clock.now.plus(Duration.ofMinutes(16));
        throttle.recordFailure();
        assertThat(throttle.isBlocked()).isFalse();
    }
}
