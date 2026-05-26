package com.dduany.intermediary.planevent;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanEventRepository extends JpaRepository<PlanEvent,Long> {
    List<PlanEvent> findByPlanItemIdOrderByEventTimeAsc(Long planItemId);
}
