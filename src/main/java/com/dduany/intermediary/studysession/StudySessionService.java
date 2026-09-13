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
        existing.setCertificationId(request.getCertificationId());
        existing.setPlanItemId(request.getPlanItemId());
        existing.setNotes(request.getNotes());

        StudySession saved = repository.saveAndFlush(existing);
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
