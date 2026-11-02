package com.dduany.intermediary.planitem;

import com.dduany.intermediary.exception.ResourceNotFoundException;
import com.dduany.intermediary.planitem.dto.PlanItemRequest;
import com.dduany.intermediary.planitem.dto.PlanItemResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

// TODO: cross-field validation that (referenceEntityType, referenceEntityId) are both set or both null.
// TODO: validate referenceEntityId points to an existing entity of the given type.
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

    /**
     * Creates or updates the item another system owns under `key`. The sender owns the title, intent,
     * date and notes. Status changes only when the request sends one (TII sends it once a day is graded),
     * so ticking today's item done in the app survives the next sync.
     */
    @Transactional
    public PlanItemResponse upsertByKey(String key, PlanItemRequest request) {
        if (key == null || key.isBlank() || key.length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "key must be 1 to 120 characters");
        }
        PlanItem item = repository.findByExternalKey(key).orElse(null);
        if (item == null) {
            item = PlanItemMapper.toEntity(request);
            item.setExternalKey(key);
            applyDefaults(item);
            return PlanItemMapper.toResponse(repository.saveAndFlush(item));
        }
        PlanItemStatus oldStatus = item.getStatus();
        item.setTitle(request.getTitle());
        item.setIntent(request.getIntent());
        item.setTargetDate(request.getTargetDate());
        item.setNotes(request.getNotes());
        if (request.getStatus() != null) {
            item.setStatus(request.getStatus());
        }
        PlanItem saved = repository.saveAndFlush(item);
        if (oldStatus != saved.getStatus()) {
            eventPublisher.publishEvent(new PlanItemStatusChangedEvent(saved.getId(), oldStatus, saved.getStatus()));
        }
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
        existing.setTargetDate(request.getTargetDate());
        existing.setStatus(request.getStatus() != null ? request.getStatus() : existing.getStatus());
        existing.setReferenceEntityType(request.getReferenceEntityType());
        existing.setReferenceEntityId(request.getReferenceEntityId());
        existing.setRecurringPlanId(request.getRecurringPlanId());
        existing.setNotes(request.getNotes());

        PlanItem saved = repository.saveAndFlush(existing);

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
