package com.dduany.intermediary.recurringplan.dto;

import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.planitem.ReferenceEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringPlanRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @NotNull(message = "Intent is required")
    private PlanIntent intent;

    @NotEmpty(message = "Pick at least one day")
    private List<DayOfWeek> days;

    private ReferenceEntityType referenceEntityType;

    private Long referenceEntityId;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;

    private Boolean active;
}
