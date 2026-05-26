package com.dduany.intermediary.planitem;

import com.dduany.intermediary.planitem.dto.PlanItemRequest;
import com.dduany.intermediary.planitem.dto.PlanItemResponse;

public final class PlanItemMapper {

    private PlanItemMapper() {
        // utility class - prevent instantiation
    }

    public static PlanItem toEntity(PlanItemRequest request){
        return PlanItem.builder()
                .title(request.getTitle())
                .intent(request.getIntent())
                .targetDate(request.getTargetDate())
                .status(request.getStatus())
                .referenceEntityType(request.getReferenceEntityType())
                .referenceEntityId(request.getReferenceEntityId())
                .notes(request.getNotes())
                .build();
    }

    public static PlanItemResponse toResponse(PlanItem entity){
        return PlanItemResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .intent(entity.getIntent())
                .targetDate(entity.getTargetDate())
                .status(entity.getStatus())
                .referenceEntityType(entity.getReferenceEntityType())
                .referenceEntityId(entity.getReferenceEntityId())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
