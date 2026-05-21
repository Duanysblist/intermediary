package com.dduany.intermediary.application.dto;

import com.dduany.intermediary.application.ApplicationSource;
import com.dduany.intermediary.application.ApplicationStatus;
import com.dduany.intermediary.application.ResumeVariant;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequest {

    @NotBlank(message = "Company is required")
    @Size(max = 200, message = "Company must be 200 characters or fewer")
    private String company;

    @NotBlank(message = "Role is required")
    @Size(max = 200, message = "Role must be 200 characters or fewer")
    private String role;

    @NotNull(message = "Application date is required")
    private LocalDate applicationDate;

    @NotNull(message = "Status is required")
    private ApplicationStatus status;

    @NotNull(message = "Source is required")
    private ApplicationSource source;

    @NotNull(message = "Resume variant is required")
    private ResumeVariant resumeVariant;

    @Size(max = 200, message = "Location must be 200 characters or fewer")
    private String location;

    @Size(max = 100, message = "Requisition ID must be 100 characters or fewer")
    private String requisitionId;

    @Size(max = 500, message = "Job URL must be 500 characters or fewer")
    private String jobUrl;

    @PositiveOrZero
    private Integer salaryRangeMin;

    @PositiveOrZero
    private Integer salaryRangeMax;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;
}
