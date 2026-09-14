package com.dduany.intermediary.studysession.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySessionResponse {

    private Long id;

    private LocalDateTime sessionDate;

    private Integer durationMinutes;

    private Long certificationId;

    private Long planItemId;

    private String notes;

    private Instant createdAt;

    private Instant updatedAt;
}
