package com.dduany.intermediary.application.dto;

import com.dduany.intermediary.application.ApplicationSource;
import com.dduany.intermediary.application.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Instant;

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

    private String resumeFile;

    private String location;

    private String requisitionId;

    private String jobUrl;

    private Integer salaryRangeMin;

    private Integer salaryRangeMax;

    private String notes;

    private String contactName;

    private LocalDate nextActionDate;

    private Instant interviewAt;

    private Instant createdAt;

    private Instant updatedAt;
}
