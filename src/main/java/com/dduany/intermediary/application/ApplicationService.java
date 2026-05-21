package com.dduany.intermediary.application;

import com.dduany.intermediary.application.dto.ApplicationRequest;
import com.dduany.intermediary.application.dto.ApplicationResponse;
import com.dduany.intermediary.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository repository;

    public ApplicationService(ApplicationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ApplicationResponse create(ApplicationRequest request){
        Application application = ApplicationMapper.toEntity(request);
        Application saved = repository.save(application);
        return ApplicationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id){
        Application application = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id " + id
                ));
        return ApplicationMapper.toResponse(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll(){
        return repository.findAll().stream()
                .map(ApplicationMapper::toResponse)
                .toList();
    }

    @Transactional
    public ApplicationResponse update(Long id, ApplicationRequest request){
        Application existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Application not found with id " + id
                ));

        existing.setCompany(request.getCompany());
        existing.setRole(request.getRole());
        existing.setApplicationDate(request.getApplicationDate());
        existing.setStatus(request.getStatus());
        existing.setSource(request.getSource());
        existing.setResumeVariant(request.getResumeVariant());

        existing.setLocation(request.getLocation() != null ? request.getLocation() : existing.getLocation());
        existing.setRequisitionId(request.getRequisitionId() != null ? request.getRequisitionId() : existing.getRequisitionId());
        existing.setJobUrl(request.getJobUrl() != null ? request.getJobUrl() : existing.getJobUrl());
        existing.setSalaryRangeMin(request.getSalaryRangeMin() != null ? request.getSalaryRangeMin() : existing.getSalaryRangeMin());
        existing.setSalaryRangeMax(request.getSalaryRangeMax() != null ? request.getSalaryRangeMax() : existing.getSalaryRangeMax());
        existing.setNotes(request.getNotes() != null ? request.getNotes() : existing.getNotes());

        Application saved = repository.save(existing);
        return ApplicationMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id){
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id " + id);
        }
        repository.deleteById(id);
    }
}
