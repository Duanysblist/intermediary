THURSDAY MAY 21 SESSION 4: StudySession + FitnessSession

Both entities are simple "event" types with the same shape:
  - id, sessionDate (LocalDateTime), durationMinutes (Integer), notes (String), timestamps
  - StudySession: add certificationId (Long, optional — defer JPA relationship to Phase 2)
  - FitnessSession: add workoutType (enum: WORKOUT_A, WORKOUT_B, OTHER)

Pattern: 7 files per entity, build them together (same shape twice).
Reference: ApplicationController is the closest template.

Verification: same 6 curl tests against /study-sessions and /fitness-sessions.

Polish TODOs from Wednesday session (do later, not Thursday):
  - DocumentService.update: change notes setter to preserve-existing pattern
  - Add @Size messages to bare @Size annotations in ApplicationRequest
