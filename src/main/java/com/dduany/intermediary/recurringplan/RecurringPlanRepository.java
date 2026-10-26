package com.dduany.intermediary.recurringplan;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.List;

public interface RecurringPlanRepository extends JpaRepository<RecurringPlan, Long> {
    /** Locked so overlapping generate calls run one after the other instead of both creating the same items. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<RecurringPlan> findByActiveTrue();
}
