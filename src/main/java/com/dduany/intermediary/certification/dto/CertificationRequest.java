package com.dduany.intermediary.certification.dto;

import com.dduany.intermediary.certification.CertificationStatus;
import jakarta.validation.constraints.NotBlank;
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
public class CertificationRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must be 200 characters or fewer")
    private String name;

    @NotBlank(message = "Vendor is required")
    @Size(max = 200, message = "Vendor name must be 200 characters or fewer")
    private String vendor;

    private CertificationStatus status;

    private LocalDate examDate;

    @PositiveOrZero(message = "Hours studied must be greater than or equal to zero")
    private Integer hoursStudied;

    @Size(max = 2000, message = "Notes must be 2000 characters or fewer")
    private String notes;


}
