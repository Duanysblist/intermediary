package com.dduany.intermediary;


import com.dduany.intermediary.planevent.PlanEventService;
import com.dduany.intermediary.planevent.dto.PlanEventResponse;
import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.planitem.PlanItemService;
import com.dduany.intermediary.planitem.PlanItemStatus;
import com.dduany.intermediary.planitem.dto.PlanItemRequest;
import com.dduany.intermediary.planitem.dto.PlanItemResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** A status change is audited exactly once. Testcontainers test: not run locally (no Docker), runs in CI. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PlanEventAuditTest {

    @Autowired PlanItemService planItemService;
    @Autowired PlanEventService planEventService;

    @Test
    void statusChangeWritesExactlyOnePlanEvent() {
        PlanItemRequest request = PlanItemRequest.builder()
                .title("Audit once")
                .intent(PlanIntent.STUDY)
                .status(PlanItemStatus.PLANNED)
                .build();
        PlanItemResponse created = planItemService.create(request);

        request.setStatus(PlanItemStatus.DONE);
        planItemService.update(created.getId(), request);

        List<PlanEventResponse> events = planEventService.findByPlanItemId(created.getId());
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getFromStatus()).isEqualTo(PlanItemStatus.PLANNED);
        assertThat(events.get(0).getToStatus()).isEqualTo(PlanItemStatus.DONE);
    }
}
