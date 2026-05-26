package com.dduany.intermediary.planitem;

import lombok.Getter;

import java.time.LocalDateTime;

/**
 * Published when a PlanItem's status changes via update.
 * Listeners can react to this event to record audit entries,
 * trigger notifications, update analytics, etc.
 * <p>
 * This event is immutable - once published, its data cannot change.*/
@Getter
public class PlanItemStatusChangedEvent {

    private final Long planItemId;
    private final PlanItemStatus fromStatus;
    private final PlanItemStatus toStatus;
    private final LocalDateTime occurredAt;

    public PlanItemStatusChangedEvent(Long planItemId, PlanItemStatus fromStatus, PlanItemStatus toStatus) {
        this.planItemId = planItemId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.occurredAt = LocalDateTime.now();
    }
}
