package com.dduany.intermediary.studysession;

import com.dduany.intermediary.exception.ResourceNotFoundException;
import com.dduany.intermediary.studysession.dto.StudySessionRequest;
import com.dduany.intermediary.studysession.dto.StudySessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudySessionService {

    // TODO: validate certificationId exists in CertificationRepository.
    // Deferred to Phase 2 along with full JPA relationship modeling.

    private final StudySessionRepository repository;

    public StudySessionService(StudySessionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public StudySessionResponse create(StudySessionRequest request){
        StudySession studySession = StudySessionMapper.toEntity(request);
        StudySession saved = repository.save(studySession);
        return StudySessionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public StudySessionResponse findById(Long id){
        StudySession studySession = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study session not found with id: " + id
                ));
        return StudySessionMapper.toResponse(studySession);
    }

    @Transactional(readOnly = true)
    public List<StudySessionResponse> findAll(){
        return repository.findAll().stream()
                .map(StudySessionMapper::toResponse)
                .toList();
    }

    @Transactional
    public StudySessionResponse update(Long id, StudySessionRequest request){
        StudySession existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Study session not found with id: " + id
                ));

        existing.setSessionDate(request.getSessionDate());
        existing.setDurationMinutes(request.getDurationMinutes());
        existing.setCertificationId(request.getCertificationId() != null ? request.getCertificationId() : existing.getCertificationId());
        existing.setNotes(request.getNotes() != null ? request.getNotes(): existing.getNotes());

        StudySession saved = repository.save(existing);
        return StudySessionMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id){
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Study session not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
