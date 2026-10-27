package com.dduany.intermediary.recurringplan;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RecurringPlanRangeTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 26);

    @Test
    void newRoutineStartsToday() {
        assertThat(RecurringPlanService.firstDateToGenerate(TODAY, null)).isEqualTo(TODAY);
    }

    @Test
    void routineResumesTheDayAfterItsLastGeneratedDate() {
        assertThat(RecurringPlanService.firstDateToGenerate(TODAY, TODAY.plusDays(14))).isEqualTo(TODAY.plusDays(15));
        assertThat(RecurringPlanService.firstDateToGenerate(TODAY, TODAY)).isEqualTo(TODAY.plusDays(1));
    }

    @Test
    void staleCoverageStartsToday() {
        assertThat(RecurringPlanService.firstDateToGenerate(TODAY, TODAY.minusDays(3))).isEqualTo(TODAY);
        assertThat(RecurringPlanService.firstDateToGenerate(TODAY, TODAY.minusDays(1))).isEqualTo(TODAY);
    }
}
