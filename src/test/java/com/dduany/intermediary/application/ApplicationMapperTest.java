package com.dduany.intermediary.application;

import com.dduany.intermediary.application.dto.ApplicationRequest;
import com.dduany.intermediary.application.dto.ApplicationResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationMapperTest {

    @Test
    void followUpFieldsAndResumeFileSurviveTheRoundTrip() {
        Instant interview = Instant.parse("2026-11-02T19:30:00Z");
        ApplicationRequest request = ApplicationRequest.builder()
                .company("Acme")
                .role("Engineer")
                .applicationDate(LocalDate.of(2026, 9, 30))
                .status(ApplicationStatus.SCREENING)
                .source(ApplicationSource.REFERRAL)
                .resumeFile("Daniel_Duany_Resume_MASTER")
                .contactName("Jess Rivera")
                .nextActionDate(LocalDate.of(2026, 11, 2))
                .interviewAt(interview)
                .build();

        ApplicationResponse response = ApplicationMapper.toResponse(ApplicationMapper.toEntity(request));

        assertThat(response.getResumeFile()).isEqualTo("Daniel_Duany_Resume_MASTER");
        assertThat(response.getContactName()).isEqualTo("Jess Rivera");
        assertThat(response.getNextActionDate()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(response.getInterviewAt()).isEqualTo(interview);
    }

    @Test
    void followUpFieldsAreOptional() {
        ApplicationRequest request = ApplicationRequest.builder()
                .company("Acme")
                .role("Engineer")
                .applicationDate(LocalDate.of(2026, 9, 30))
                .status(ApplicationStatus.APPLIED)
                .source(ApplicationSource.COLD)
                .resumeFile("Variant A - Defense")
                .build();

        ApplicationResponse response = ApplicationMapper.toResponse(ApplicationMapper.toEntity(request));

        assertThat(response.getContactName()).isNull();
        assertThat(response.getNextActionDate()).isNull();
        assertThat(response.getInterviewAt()).isNull();
    }
}
