package com.dduany.intermediary.planevent;

import com.dduany.intermediary.exception.ResourceNotFoundException;
import com.dduany.intermediary.planevent.dto.PlanEventRequest;
import com.dduany.intermediary.planevent.dto.PlanEventResponse;
import com.dduany.intermediary.planitem.PlanItemStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlanEventService {

    private final PlanEventRepository repository;

    public PlanEventService(PlanEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PlanEventResponse create(PlanEventRequest request) {
        PlanEvent event = PlanEventMapper.toEntity(request);
        PlanEvent saved = repository.save(event);
        return PlanEventMapper.toResponse(saved);
    }

    /**
     * Called by PlanEventListener when a PlanItem status change is observed.
     * Bypasses the request DTO since the data comes from internal events, not HTTP.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordTransition(Long planItemId, PlanItemStatus fromStatus, PlanItemStatus toStatus, String notes) {

            PlanEvent event = PlanEvent.builder()
                    .planItemId(planItemId)
                    .fromStatus(fromStatus)
                    .toStatus(toStatus)
                    .eventTime(LocalDateTime.now())
                    .notes(notes)
                    .build();

            repository.save(event);

    }

    @Transactional(readOnly = true)
    public PlanEventResponse findById(Long id) {
        PlanEvent event = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan event not found with id: " + id
                ));
        return PlanEventMapper.toResponse(event);
    }

    @Transactional(readOnly = true)
    public List<PlanEventResponse> findAll() {
        return repository.findAll().stream()
                .map(PlanEventMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlanEventResponse> findByPlanItemId(Long planItemId) {
        return repository.findByPlanItemIdOrderByEventTimeAsc(planItemId).stream()
                .map(PlanEventMapper::toResponse)
                .toList();
    }
}