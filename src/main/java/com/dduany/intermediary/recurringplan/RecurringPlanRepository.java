package com.dduany.intermediary.recurringplan;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecurringPlanRepository extends JpaRepository<RecurringPlan, Long> {
    List<RecurringPlan> findByActiveTrue();
}
