package com.dduany.intermediary.planevent.dto;

import com.dduany.intermediary.planitem.PlanItemStatus;
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
public class PlanEventResponse {

    private Long id;

    private Long planItemId;

    private PlanItemStatus fromStatus;

    private PlanItemStatus toStatus;

    private LocalDateTime eventTime;

    private String notes;

    private LocalDateTime createdAt;

}
