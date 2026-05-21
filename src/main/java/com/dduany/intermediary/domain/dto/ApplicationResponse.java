package com.dduany.intermediary.domain.dto;

import com.dduany.intermediary.domain.ApplicationSource;
import com.dduany.intermediary.domain.ApplicationStatus;
import com.dduany.intermediary.domain.ResumeVariant;
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
public class ApplicationResponse {

    private Long id;

    private String company;

    private String role;

    private LocalDate applicationDate;

    private ApplicationStatus status;

    private ApplicationSource source;

    private ResumeVariant resumeVariant;

    private String location;

    private String requisitionId;

    private String jobUrl;

    private Integer salaryRangeMin;

    private Integer salaryRangeMax;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
