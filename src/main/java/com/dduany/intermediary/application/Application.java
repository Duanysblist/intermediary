package com.dduany.intermediary.application;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company")
    private String company;

    @Column(name = "role")
    private String role;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "source")
    private ApplicationSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "resume_variant")
    private ResumeVariant resumeVariant;

    @Column(name = "location")
    private String location;

    @Column(name = "requisition_id")
    private String requisitionId;

    @Column(name = "job_url")
    private String jobUrl;

    // TODO: cross-field validation that salaryRangeMax >= salaryRangeMin
    // Skipped for now - requires custom validator.
    @Column(name = "salary_range_min")
    private Integer salaryRangeMin;

    @Column(name = "salary_range_max")
    private Integer salaryRangeMax;

    @Column(name = "notes", length = 2000)
    private String notes;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
