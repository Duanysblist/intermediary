package com.dduany.intermediary.fitnesssession;

import com.dduany.intermediary.fitnesssession.dto.FitnessSessionRequest;
import com.dduany.intermediary.fitnesssession.dto.FitnessSessionResponse;

public final class FitnessSessionMapper {

    private FitnessSessionMapper() {
        // utility class - prevent instantiation
    }

    public static FitnessSession toEntity(FitnessSessionRequest request) {
        return FitnessSession.builder()
                .sessionDate(request.getSessionDate())
                .durationMinutes(request.getDurationMinutes())
                .workoutType(request.getWorkoutType())
                .notes(request.getNotes())
                .build();
    }

    public static FitnessSessionResponse toResponse(FitnessSession entity) {
        return FitnessSessionResponse.builder()
                .id(entity.getId())
                .sessionDate(entity.getSessionDate())
                .durationMinutes(entity.getDurationMinutes())
                .workoutType(entity.getWorkoutType())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
