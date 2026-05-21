package com.dduany.intermediary.certification;

import com.dduany.intermediary.certification.dto.CertificationRequest;
import com.dduany.intermediary.certification.dto.CertificationResponse;
import com.dduany.intermediary.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CertificationService {

    private final CertificationRepository repository;

    private static final CertificationStatus DEFAULT_CERT_STATUS = CertificationStatus.PLANNING;
    private static final Integer DEFAULT_HOURS_STUDIED = 0;

    public CertificationService(CertificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CertificationResponse create(CertificationRequest request) {

        Certification certification = CertificationMapper.toEntity(request);
        applyDefaults(certification);
        Certification saved = repository.save(certification);
        return CertificationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CertificationResponse findById(Long id) {
        Certification certification = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certification not found with id: " + id
                ));
        return CertificationMapper.toResponse(certification);
    }

    @Transactional(readOnly = true)
    public List<CertificationResponse> findAll() {
        return repository.findAll().stream()
                .map(CertificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public CertificationResponse update(Long id, CertificationRequest request) {
        Certification existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Certification not found with id: " + id
                ));

        existing.setName(request.getName());
        existing.setVendor(request.getVendor());
        existing.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());
        existing.setExamDate(request.getExamDate());
        existing.setHoursStudied(request.getHoursStudied() != null ? request.getHoursStudied() : existing.getHoursStudied());
        existing.setNotes(request.getNotes());

        Certification saved = repository.save(existing);
        return CertificationMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Certification not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private void applyDefaults(Certification certification) {
        if (certification.getStatus() == null) {
            certification.setStatus(DEFAULT_CERT_STATUS);
        }
        if (certification.getHoursStudied() == null) {
            certification.setHoursStudied(DEFAULT_HOURS_STUDIED);
        }
    }
}
