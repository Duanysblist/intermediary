package com.dduany.intermediary.certification.dto;

import com.dduany.intermediary.certification.CertificationStatus;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationResponse {

    private Long id;

    private String name;

    private String vendor;

    private CertificationStatus status;

    private LocalDate examDate;

    private Integer hoursStudied;

    private String notes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
