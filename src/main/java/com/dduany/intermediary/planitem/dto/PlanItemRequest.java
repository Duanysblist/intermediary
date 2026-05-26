package com.dduany.intermediary.planitem.dto;

import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.planitem.PlanItemStatus;
import com.dduany.intermediary.planitem.ReferenceEntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanItemRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @NotNull(message = "PlanIntent is required")
    private PlanIntent intent;

    private LocalDate targetDate;

    private PlanItemStatus status;

    private ReferenceEntityType referenceEntityType;

    private Long referenceEntityId;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;
}
