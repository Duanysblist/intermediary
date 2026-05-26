package com.dduany.intermediary.fitnesssession.dto;

import com.dduany.intermediary.fitnesssession.WorkoutType;
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
public class FitnessSessionResponse {

    private Long id;

    private LocalDateTime sessionDate;

    private Integer durationMinutes;

    private WorkoutType workoutType;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
