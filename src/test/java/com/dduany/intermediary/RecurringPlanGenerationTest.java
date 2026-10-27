package com.dduany.intermediary;


import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.recurringplan.RecurringPlanService;
import com.dduany.intermediary.planitem.PlanItemRepository;
import com.dduany.intermediary.planitem.PlanItemService;
import com.dduany.intermediary.planitem.dto.PlanItemResponse;
import com.dduany.intermediary.recurringplan.dto.RecurringPlanRequest;
import com.dduany.intermediary.recurringplan.dto.RecurringPlanResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.DayOfWeek;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Generation must not recreate deleted items or duplicate items. Testcontainers test: not run locally (no Docker), runs in CI. */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class RecurringPlanGenerationTest {

    @Autowired RecurringPlanService recurringPlanService;
    @Autowired PlanItemService planItemService;
    @Autowired PlanItemRepository planItemRepository;

    private RecurringPlanResponse everyDayRoutine(String title) {
        return recurringPlanService.create(RecurringPlanRequest.builder()
                .title(title)
                .intent(PlanIntent.EXERCISE)
                .days(Arrays.asList(DayOfWeek.values()))
                .build());
    }

    private static List<PlanItemResponse> itemsOf(List<PlanItemResponse> items, Long planId) {
        return items.stream().filter(i -> planId.equals(i.getRecurringPlanId())).toList();
    }

    @Test
    void deletedItemDoesNotComeBackOnNextGenerate() {
        RecurringPlanResponse plan = everyDayRoutine("Deleted routine");
        PlanItemResponse victim = itemsOf(recurringPlanService.generate(7), plan.getId()).get(0);

        planItemService.delete(victim.getId());
        List<PlanItemResponse> again = recurringPlanService.generate(7);

        assertThat(itemsOf(again, plan.getId())).isEmpty();
        assertThat(planItemRepository.findAll().stream()
                .filter(p -> plan.getId().equals(p.getRecurringPlanId()) && victim.getTargetDate().equals(p.getTargetDate()))
                .count()).isZero();
    }

    @Test
    void generatingTwiceCreatesNoDuplicates() {
        RecurringPlanResponse plan = everyDayRoutine("Twice routine");
        assertThat(itemsOf(recurringPlanService.generate(7), plan.getId())).hasSize(8);

        assertThat(itemsOf(recurringPlanService.generate(7), plan.getId())).isEmpty();
        assertThat(planItemRepository.findAll().stream().filter(p -> plan.getId().equals(p.getRecurringPlanId())).count()).isEqualTo(8);
    }
}
