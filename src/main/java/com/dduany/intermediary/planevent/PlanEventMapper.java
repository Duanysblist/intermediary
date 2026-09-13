package com.dduany.intermediary.planevent;

import com.dduany.intermediary.planevent.dto.PlanEventResponse;

public final class PlanEventMapper {

    private PlanEventMapper() {
        // utility class - prevent instantiation
    }

    public static PlanEventResponse toResponse(PlanEvent entity) {
        return PlanEventResponse.builder()
                .id(entity.getId())
                .planItemId(entity.getPlanItemId())
                .fromStatus(entity.getFromStatus())
                .toStatus(entity.getToStatus())
                .eventTime(entity.getEventTime())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
