package com.dduany.intermediary;

import com.dduany.intermediary.application.ApplicationService;
import com.dduany.intermediary.application.ApplicationSource;
import com.dduany.intermediary.application.ApplicationStatus;
import com.dduany.intermediary.application.ResumeVariant;
import com.dduany.intermediary.application.dto.ApplicationRequest;
import com.dduany.intermediary.application.dto.ApplicationResponse;
import com.dduany.intermediary.certification.CertificationService;
import com.dduany.intermediary.certification.CertificationStatus;
import com.dduany.intermediary.certification.dto.CertificationRequest;
import com.dduany.intermediary.certification.dto.CertificationResponse;
import com.dduany.intermediary.document.DocumentService;
import com.dduany.intermediary.document.DocumentType;
import com.dduany.intermediary.document.dto.DocumentRequest;
import com.dduany.intermediary.document.dto.DocumentResponse;
import com.dduany.intermediary.fitnesssession.FitnessSessionService;
import com.dduany.intermediary.fitnesssession.WorkoutType;
import com.dduany.intermediary.fitnesssession.dto.FitnessSessionRequest;
import com.dduany.intermediary.fitnesssession.dto.FitnessSessionResponse;
import com.dduany.intermediary.planitem.PlanIntent;
import com.dduany.intermediary.planitem.PlanItemService;
import com.dduany.intermediary.planitem.PlanItemStatus;
import com.dduany.intermediary.planitem.dto.PlanItemRequest;
import com.dduany.intermediary.planitem.dto.PlanItemResponse;
import com.dduany.intermediary.studysession.StudySessionService;
import com.dduany.intermediary.studysession.dto.StudySessionRequest;
import com.dduany.intermediary.studysession.dto.StudySessionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression test for the "updatedAt never changes" bug: the service update
 * methods mapped the response before Hibernate flushed the UPDATE, so the
 * {@code @UpdateTimestamp} value in the response was still the insert-time value.
 *
 * Each test creates an entity, updates it through the service (the same code path
 * as PUT /{resource}/{id}) and asserts that the response's updatedAt is strictly
 * after createdAt, and that a fresh read from the database agrees.
 *
 * Tests are deliberately NOT @Transactional so each service call commits exactly
 * as it does behind the REST controllers.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class UpdatedAtTimestampTest {

	/** Small gap so createdAt and updatedAt cannot land on the same clock tick. */
	private static final long CLOCK_GAP_MS = 20;

	@Autowired PlanItemService planItemService;
	@Autowired ApplicationService applicationService;
	@Autowired CertificationService certificationService;
	@Autowired DocumentService documentService;
	@Autowired FitnessSessionService fitnessSessionService;
	@Autowired StudySessionService studySessionService;

	@Test
	void planItemUpdateAdvancesUpdatedAt() throws InterruptedException {
		PlanItemRequest request = PlanItemRequest.builder()
				.title("updatedAt regression")
				.intent(PlanIntent.STUDY)
				.status(PlanItemStatus.PLANNED)
				.build();
		PlanItemResponse created = planItemService.create(request);
		try {
			Thread.sleep(CLOCK_GAP_MS);
			request.setStatus(PlanItemStatus.DONE);

			PlanItemResponse updated = planItemService.update(created.getId(), request);

			assertThat(updated.getStatus()).isEqualTo(PlanItemStatus.DONE);
			assertUpdatedAfterCreated(updated.getCreatedAt(), updated.getUpdatedAt(), "PUT response");
			PlanItemResponse reloaded = planItemService.findById(created.getId());
			assertUpdatedAfterCreated(reloaded.getCreatedAt(), reloaded.getUpdatedAt(), "reloaded");
		} finally {
			planItemService.delete(created.getId());
		}
	}

	@Test
	void applicationUpdateAdvancesUpdatedAt() throws InterruptedException {
		ApplicationRequest request = ApplicationRequest.builder()
				.company("Acme")
				.role("Engineer")
				.applicationDate(LocalDate.of(2026, 9, 1))
				.status(ApplicationStatus.APPLIED)
				.source(ApplicationSource.COLD)
				.resumeVariant(ResumeVariant.VARIANT_A_DEFENSE)
				.build();
		ApplicationResponse created = applicationService.create(request);
		try {
			Thread.sleep(CLOCK_GAP_MS);
			request.setStatus(ApplicationStatus.SCREENING);

			ApplicationResponse updated = applicationService.update(created.getId(), request);

			assertUpdatedAfterCreated(updated.getCreatedAt(), updated.getUpdatedAt(), "PUT response");
			ApplicationResponse reloaded = applicationService.findById(created.getId());
			assertUpdatedAfterCreated(reloaded.getCreatedAt(), reloaded.getUpdatedAt(), "reloaded");
		} finally {
			applicationService.delete(created.getId());
		}
	}

	@Test
	void certificationUpdateAdvancesUpdatedAt() throws InterruptedException {
		CertificationRequest request = CertificationRequest.builder()
				.name("Security+")
				.vendor("CompTIA")
				.status(CertificationStatus.PLANNING)
				.build();
		CertificationResponse created = certificationService.create(request);
		try {
			Thread.sleep(CLOCK_GAP_MS);
			request.setStatus(CertificationStatus.STUDYING);

			CertificationResponse updated = certificationService.update(created.getId(), request);

			assertUpdatedAfterCreated(updated.getCreatedAt(), updated.getUpdatedAt(), "PUT response");
			CertificationResponse reloaded = certificationService.findById(created.getId());
			assertUpdatedAfterCreated(reloaded.getCreatedAt(), reloaded.getUpdatedAt(), "reloaded");
		} finally {
			certificationService.delete(created.getId());
		}
	}

	@Test
	void documentUpdateAdvancesUpdatedAt() throws InterruptedException {
		DocumentRequest request = DocumentRequest.builder()
				.title("Resume")
				.path("/docs/resume.pdf")
				.type(DocumentType.RESUME)
				.build();
		DocumentResponse created = documentService.create(request);
		try {
			Thread.sleep(CLOCK_GAP_MS);
			request.setTitle("Resume v2");

			DocumentResponse updated = documentService.update(created.getId(), request);

			assertUpdatedAfterCreated(updated.getCreatedAt(), updated.getUpdatedAt(), "PUT response");
			DocumentResponse reloaded = documentService.findById(created.getId());
			assertUpdatedAfterCreated(reloaded.getCreatedAt(), reloaded.getUpdatedAt(), "reloaded");
		} finally {
			documentService.delete(created.getId());
		}
	}

	@Test
	void fitnessSessionUpdateAdvancesUpdatedAt() throws InterruptedException {
		FitnessSessionRequest request = FitnessSessionRequest.builder()
				.sessionDate(LocalDateTime.of(2026, 9, 1, 7, 0))
				.durationMinutes(30)
				.workoutType(WorkoutType.WALK)
				.build();
		FitnessSessionResponse created = fitnessSessionService.create(request);
		try {
			Thread.sleep(CLOCK_GAP_MS);
			request.setDurationMinutes(45);

			FitnessSessionResponse updated = fitnessSessionService.update(created.getId(), request);

			assertUpdatedAfterCreated(updated.getCreatedAt(), updated.getUpdatedAt(), "PUT response");
			FitnessSessionResponse reloaded = fitnessSessionService.findById(created.getId());
			assertUpdatedAfterCreated(reloaded.getCreatedAt(), reloaded.getUpdatedAt(), "reloaded");
		} finally {
			fitnessSessionService.delete(created.getId());
		}
	}

	@Test
	void studySessionUpdateAdvancesUpdatedAt() throws InterruptedException {
		StudySessionRequest request = StudySessionRequest.builder()
				.sessionDate(LocalDateTime.of(2026, 9, 1, 19, 0))
				.durationMinutes(60)
				.build();
		StudySessionResponse created = studySessionService.create(request);
		try {
			Thread.sleep(CLOCK_GAP_MS);
			request.setDurationMinutes(90);

			StudySessionResponse updated = studySessionService.update(created.getId(), request);

			assertUpdatedAfterCreated(updated.getCreatedAt(), updated.getUpdatedAt(), "PUT response");
			StudySessionResponse reloaded = studySessionService.findById(created.getId());
			assertUpdatedAfterCreated(reloaded.getCreatedAt(), reloaded.getUpdatedAt(), "reloaded");
		} finally {
			studySessionService.delete(created.getId());
		}
	}

	private static void assertUpdatedAfterCreated(Instant createdAt, Instant updatedAt, String source) {
		assertThat(createdAt).as("%s createdAt", source).isNotNull();
		assertThat(updatedAt).as("%s updatedAt", source).isNotNull();
		assertThat(updatedAt)
				.as("%s: updatedAt should be after createdAt", source)
				.isAfter(createdAt);
	}
}
