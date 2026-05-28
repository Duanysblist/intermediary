package com.dduany.intermediary.fitnesssession;

import com.dduany.intermediary.exception.ResourceNotFoundException;
import com.dduany.intermediary.fitnesssession.dto.FitnessSessionRequest;
import com.dduany.intermediary.fitnesssession.dto.FitnessSessionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FitnessSessionService {

    private final FitnessSessionRepository repository;

    public FitnessSessionService(FitnessSessionRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FitnessSessionResponse create(FitnessSessionRequest request) {
        FitnessSession fitnessSession = FitnessSessionMapper.toEntity(request);
        FitnessSession saved = repository.save(fitnessSession);
        return FitnessSessionMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public FitnessSessionResponse findById(Long id) {
        FitnessSession fitnessSession = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fitness session not found with id: " + id
                ));
        return FitnessSessionMapper.toResponse(fitnessSession);
    }

    @Transactional(readOnly = true)
    public List<FitnessSessionResponse> findAll() {
        return repository.findAll().stream()
                .map(FitnessSessionMapper::toResponse)
                .toList();
    }

    @Transactional
    public FitnessSessionResponse update(Long id, FitnessSessionRequest request) {
        FitnessSession existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fitness session not found with id: " + id
                ));

        existing.setSessionDate(request.getSessionDate());
        existing.setDurationMinutes(request.getDurationMinutes());
        existing.setWorkoutType(request.getWorkoutType());
        existing.setNotes(request.getNotes() != null ? request.getNotes() : existing.getNotes());

        FitnessSession saved = repository.save(existing);
        return FitnessSessionMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Fitness session not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
