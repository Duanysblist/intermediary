package com.dduany.intermediary.studysession;

import com.dduany.intermediary.studysession.dto.StudySessionRequest;
import com.dduany.intermediary.studysession.dto.StudySessionResponse;

public final class StudySessionMapper {

    private StudySessionMapper(){
        // utility class - prevent instantiation
    }

    public static StudySession toEntity(StudySessionRequest request){
        return StudySession.builder()
                .sessionDate(request.getSessionDate())
                .durationMinutes(request.getDurationMinutes())
                .certificationId(request.getCertificationId())
                .notes(request.getNotes())
                .build();
    }

    public static StudySessionResponse toResponse(StudySession entity){
        return StudySessionResponse.builder()
                .id(entity.getId())
                .sessionDate(entity.getSessionDate())
                .durationMinutes(entity.getDurationMinutes())
                .certificationId(entity.getCertificationId())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

}
