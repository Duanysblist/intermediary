package com.dduany.intermediary.planitem.dto;

import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.planitem.PlanItemStatus;
import com.dduany.intermediary.planitem.ReferenceEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanItemResponse {

    private Long id;

    private String title;

    private PlanIntent intent;

    private LocalDate targetDate;

    private PlanItemStatus status;

    private ReferenceEntityType referenceEntityType;

    private Long referenceEntityId;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
