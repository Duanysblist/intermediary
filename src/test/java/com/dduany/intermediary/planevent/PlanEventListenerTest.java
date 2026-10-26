package com.dduany.intermediary.planevent;

import com.dduany.intermediary.planitem.PlanItemStatus;
import com.dduany.intermediary.planitem.PlanItemStatusChangedEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class PlanEventListenerTest {

    @Test
    void auditFailureIsLoggedNotThrownToTheCaller() {
        PlanEventService service = mock(PlanEventService.class);
        doThrow(new RuntimeException("insert failed")).when(service).recordTransition(anyLong(), any(), any(), any());
        PlanEventListener listener = new PlanEventListener(service);

        assertThatCode(() -> listener.handleStatusChange(
                new PlanItemStatusChangedEvent(7L, PlanItemStatus.PLANNED, PlanItemStatus.DONE)))
                .doesNotThrowAnyException();
    }
}
