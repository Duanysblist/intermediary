package com.dduany.intermediary.domain;

import com.dduany.intermediary.domain.dto.CertificationRequest;
import com.dduany.intermediary.domain.dto.CertificationResponse;

public final class CertificationMapper {

    private CertificationMapper() {
        // utility class - prevent instantiation
    }

    public static Certification toEntity(CertificationRequest request) {
        return Certification.builder()
                .name(request.getName())
                .vendor(request.getVendor())
                .status(request.getStatus())
                .examDate(request.getExamDate())
                .hoursStudied(request.getHoursStudied())
                .notes(request.getNotes())
                .build();
    }

    public static CertificationResponse toResponse(Certification entity) {
        return CertificationResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .vendor(entity.getVendor())
                .status(entity.getStatus())
                .examDate(entity.getExamDate())
                .hoursStudied(entity.getHoursStudied())
                .notes(entity.getNotes())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
