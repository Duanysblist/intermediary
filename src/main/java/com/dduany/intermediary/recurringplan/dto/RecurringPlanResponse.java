package com.dduany.intermediary.recurringplan.dto;

import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.planitem.ReferenceEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPlanResponse {
    private Long id;
    private String title;
    private PlanIntent intent;
    private List<DayOfWeek> days;
    private ReferenceEntityType referenceEntityType;
    private Long referenceEntityId;
    private String notes;
    private boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
