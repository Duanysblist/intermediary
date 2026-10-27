package com.dduany.intermediary.auth;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Blocks sign-in after too many failures in a sliding window. In memory, so a restart clears it;
 * this is a single-account app, so one shared counter is enough.
 */
@Component
public class LoginThrottle {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);

    private final Clock clock;
    private final Deque<Instant> failures = new ArrayDeque<>();

    public LoginThrottle(Clock clock) {
        this.clock = clock;
    }

    /** True while the window holds {@link #MAX_FAILURES} or more failures. */
    public synchronized boolean isBlocked() {
        prune();
        return failures.size() >= MAX_FAILURES;
    }

    public synchronized void recordFailure() {
        prune();
        failures.addLast(clock.instant());
    }

    /** A successful sign-in clears the count. */
    public synchronized void reset() {
        failures.clear();
    }

    private void prune() {
        Instant cutoff = clock.instant().minus(WINDOW);
        while (!failures.isEmpty() && failures.peekFirst().isBefore(cutoff)) failures.pollFirst();
    }
}
