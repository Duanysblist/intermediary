package com.dduany.intermediary.application;

import com.dduany.intermediary.application.dto.ApplicationRequest;
import com.dduany.intermediary.application.dto.ApplicationResponse;

public final class ApplicationMapper {

    private ApplicationMapper() {
        // utility class - prevent instantiation
    }

    public static Application toEntity(ApplicationRequest request) {
        return Application.builder()
                .company(request.getCompany())
                .role(request.getRole())
                .applicationDate(request.getApplicationDate())
                .status(request.getStatus())
                .source(request.getSource())
                .resumeFile(request.getResumeFile())
                .location(request.getLocation())
                .requisitionId(request.getRequisitionId())
                .jobUrl(request.getJobUrl())
                .salaryRangeMin(request.getSalaryRangeMin())
                .salaryRangeMax(request.getSalaryRangeMax())
                .notes(request.getNotes())
                .contactName(request.getContactName())
                .nextActionDate(request.getNextActionDate())
                .interviewAt(request.getInterviewAt())
                .build();
    }

    public static ApplicationResponse toResponse(Application entity) {
        return ApplicationResponse.builder()
                .id(entity.getId())
                .company(entity.getCompany())
                .role(entity.getRole())
                .applicationDate(entity.getApplicationDate())
                .status(entity.getStatus())
                .source(entity.getSource())
                .resumeFile(entity.getResumeFile())
                .location(entity.getLocation())
                .requisitionId(entity.getRequisitionId())
                .jobUrl(entity.getJobUrl())
                .salaryRangeMin(entity.getSalaryRangeMin())
                .salaryRangeMax(entity.getSalaryRangeMax())
                .notes(entity.getNotes())
                .contactName(entity.getContactName())
                .nextActionDate(entity.getNextActionDate())
                .interviewAt(entity.getInterviewAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
