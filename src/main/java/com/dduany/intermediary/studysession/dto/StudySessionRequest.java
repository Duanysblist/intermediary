package com.dduany.intermediary.studysession.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySessionRequest {

    @NotNull(message = "Session date is required")
    private LocalDateTime sessionDate;

    @NotNull(message = "Duration minutes is required")
    @Positive
    private Integer durationMinutes;

    private Long certificationId;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;
}
