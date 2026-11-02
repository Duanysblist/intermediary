package com.dduany.intermediary.planitem.dto;

import com.dduany.intermediary.planitem.PlanItemStatus;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/** One board column after a drag: every id ends up in `status`, at its index in `ids`. */
public record PlanItemOrderRequest(
        @NotNull(message = "Status is required") PlanItemStatus status,
        @NotNull(message = "Ids are required") List<@NotNull Long> ids
) {
}
