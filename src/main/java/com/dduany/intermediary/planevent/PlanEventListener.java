package com.dduany.intermediary.planevent;

import com.dduany.intermediary.planitem.PlanItemStatusChangedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Listens for PlanItem status changes and records them as PlanEvents.
 * <p>
 * Uses TransactionPhase.AFTER_COMMIT to ensure audit entries only exist
 * for changes that actually committed to the database. This prevents
 * phantom audit entries from rolled-back transactions.
 */
@Component
public class PlanEventListener {

    private final PlanEventService planEventService;

    public PlanEventListener(PlanEventService planEventService) {
        this.planEventService = planEventService;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStatusChange(PlanItemStatusChangedEvent event) {
        planEventService.recordTransition(
                event.getPlanItemId(),
                event.getFromStatus(),
                event.getToStatus(),
                null // notes - null for auto-generated events
        );
    }
}
