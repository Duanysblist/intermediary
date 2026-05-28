package com.dduany.intermediary.planitem;

import com.dduany.intermediary.exception.ResourceNotFoundException;
import com.dduany.intermediary.planitem.dto.PlanItemRequest;
import com.dduany.intermediary.planitem.dto.PlanItemResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// TODO: cross-field validation that (referenceEntityType, referenceEntityId) are both set
// or both null. Currently a client can send just one. Deferred to Phase 2.
//
// TODO: validate referenceEntityId points to an existing entity of the given type.
// Deferred to Phase 2 along with full JPA relationship modeling.
@Service
public class PlanItemService {

    private final PlanItemRepository repository;
    private final ApplicationEventPublisher eventPublisher;

    private static final PlanItemStatus DEFAULT_PLAN_ITEM_STATUS = PlanItemStatus.PLANNED;

    public PlanItemService(PlanItemRepository repository, ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PlanItemResponse create(PlanItemRequest request) {
        PlanItem planItem = PlanItemMapper.toEntity(request);
        applyDefaults(planItem);
        PlanItem saved = repository.save(planItem);
        return PlanItemMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public PlanItemResponse findById(Long id){
        PlanItem planItem = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan item not found with id: " + id
                ));
        return PlanItemMapper.toResponse(planItem);
    }

    @Transactional(readOnly = true)
    public List<PlanItemResponse> findAll(){
        return repository.findAll().stream()
                .map(PlanItemMapper::toResponse)
                .toList();
    }

    @Transactional
    public PlanItemResponse update(Long id, PlanItemRequest request) {
        PlanItem existing = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan item not found with id: " + id
                ));

        // capture BEFORE setting
        PlanItemStatus oldStatus = existing.getStatus();

        existing.setTitle(request.getTitle());
        existing.setIntent(request.getIntent());
        existing.setTargetDate(request.getTargetDate() != null ? request.getTargetDate() : existing.getTargetDate());
        existing.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());
        existing.setReferenceEntityType(request.getReferenceEntityType() != null ? request.getReferenceEntityType() : existing.getReferenceEntityType());
        existing.setReferenceEntityId(request.getReferenceEntityId() != null ? request.getReferenceEntityId() : existing.getReferenceEntityId());
        existing.setNotes(request.getNotes() != null ? request.getNotes() : existing.getNotes());

        PlanItem saved = repository.save(existing);

        PlanItemStatus newStatus = saved.getStatus();
        if (oldStatus != newStatus){
            eventPublisher.publishEvent(new PlanItemStatusChangedEvent(
                    saved.getId(),
                    oldStatus,
                    newStatus
            ));
        }

        return PlanItemMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id){
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Plan item not found with id: " + id);
        }
        repository.deleteById(id);
    }

    private void applyDefaults(PlanItem planItem) {
        if (planItem.getStatus() == null) {
            planItem.setStatus(DEFAULT_PLAN_ITEM_STATUS);
        }
    }
}
