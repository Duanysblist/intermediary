package com.dduany.intermediary.planitem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface PlanItemRepository extends JpaRepository<PlanItem, Long> {
    boolean existsByRecurringPlanIdAndTargetDate(Long recurringPlanId, LocalDate targetDate);

    Optional<PlanItem> findByExternalKey(String externalKey);
}
