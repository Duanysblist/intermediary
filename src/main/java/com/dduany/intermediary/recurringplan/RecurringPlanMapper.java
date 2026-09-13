package com.dduany.intermediary.recurringplan;

import com.dduany.intermediary.recurringplan.dto.RecurringPlanRequest;
import com.dduany.intermediary.recurringplan.dto.RecurringPlanResponse;

public final class RecurringPlanMapper {

    private RecurringPlanMapper() {
        // utility class - prevent instantiation
    }

    public static RecurringPlan toEntity(RecurringPlanRequest request) {
        return RecurringPlan.builder()
                .title(request.getTitle())
                .intent(request.getIntent())
                .days(request.getDays())
                .referenceEntityType(request.getReferenceEntityType())
                .referenceEntityId(request.getReferenceEntityId())
                .notes(request.getNotes())
                .active(request.getActive() == null || request.getActive())
                .build();
    }

    public static RecurringPlanResponse toResponse(RecurringPlan entity) {
        return RecurringPlanResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .intent(entity.getIntent())
                .days(entity.getDays())
                .referenceEntityType(entity.getReferenceEntityType())
                .referenceEntityId(entity.getReferenceEntityId())
                .notes(entity.getNotes())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
