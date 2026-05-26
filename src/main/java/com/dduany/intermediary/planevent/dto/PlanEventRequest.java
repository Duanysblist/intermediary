package com.dduany.intermediary.planevent.dto;

import com.dduany.intermediary.planitem.PlanItemStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanEventRequest {

    @NotNull(message = "Plan item id is required")
    private Long planItemId;

    private PlanItemStatus fromStatus;

    @NotNull(message = "To status is required")
    private PlanItemStatus toStatus;

    @NotNull(message = "Event time is required")
    private LocalDateTime eventTime;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;
}
