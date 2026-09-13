package com.dduany.intermediary.recurringplan;

import com.dduany.intermediary.exception.ResourceNotFoundException;
import com.dduany.intermediary.planitem.PlanItemRepository;
import com.dduany.intermediary.planitem.PlanItemService;
import com.dduany.intermediary.planitem.PlanItemStatus;
import com.dduany.intermediary.planitem.dto.PlanItemRequest;
import com.dduany.intermediary.planitem.dto.PlanItemResponse;
import com.dduany.intermediary.recurringplan.dto.RecurringPlanRequest;
import com.dduany.intermediary.recurringplan.dto.RecurringPlanResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecurringPlanService {

    private final RecurringPlanRepository repository;
    private final PlanItemRepository planItemRepository;
    private final PlanItemService planItemService;

    public RecurringPlanService(RecurringPlanRepository repository, PlanItemRepository planItemRepository, PlanItemService planItemService) {
        this.repository = repository;
        this.planItemRepository = planItemRepository;
        this.planItemService = planItemService;
    }

    @Transactional
    public RecurringPlanResponse create(RecurringPlanRequest request) {
        return RecurringPlanMapper.toResponse(repository.save(RecurringPlanMapper.toEntity(request)));
    }

    @Transactional(readOnly = true)
    public RecurringPlanResponse findById(Long id) {
        return RecurringPlanMapper.toResponse(get(id));
    }

    @Transactional(readOnly = true)
    public List<RecurringPlanResponse> findAll() {
        return repository.findAll().stream().map(RecurringPlanMapper::toResponse).toList();
    }

    @Transactional
    public RecurringPlanResponse update(Long id, RecurringPlanRequest request) {
        RecurringPlan existing = get(id);
        existing.setTitle(request.getTitle());
        existing.setIntent(request.getIntent());
        existing.setDays(request.getDays());
        existing.setReferenceEntityType(request.getReferenceEntityType());
        existing.setReferenceEntityId(request.getReferenceEntityId());
        existing.setNotes(request.getNotes());
        if (request.getActive() != null) existing.setActive(request.getActive());
        return RecurringPlanMapper.toResponse(repository.saveAndFlush(existing));
    }

    /** Deleting a routine keeps the plan items it already generated; they are real intentions now. */
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Recurring plan not found with id: " + id);
        repository.deleteById(id);
    }

    /**
     * Creates plan items for every active routine on each matching day from today through
     * today + {@code days}. Idempotent: a routine gets at most one item per date, so this can run
     * as often as the user likes.
     */
    @Transactional
    public List<PlanItemResponse> generate(int days) {
        int horizon = Math.max(0, Math.min(days, 60));
        LocalDate today = LocalDate.now();
        List<PlanItemResponse> created = new ArrayList<>();
        for (RecurringPlan plan : repository.findByActiveTrue()) {
            for (int offset = 0; offset <= horizon; offset++) {
                LocalDate date = today.plusDays(offset);
                if (!plan.getDays().contains(date.getDayOfWeek())) continue;
                if (planItemRepository.existsByRecurringPlanIdAndTargetDate(plan.getId(), date)) continue;
                PlanItemRequest request = PlanItemRequest.builder()
                        .title(plan.getTitle())
                        .intent(plan.getIntent())
                        .targetDate(date)
                        .status(PlanItemStatus.PLANNED)
                        .referenceEntityType(plan.getReferenceEntityType())
                        .referenceEntityId(plan.getReferenceEntityId())
                        .recurringPlanId(plan.getId())
                        .notes(plan.getNotes())
                        .build();
                created.add(planItemService.create(request));
            }
        }
        return created;
    }

    private RecurringPlan get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Recurring plan not found with id: " + id));
    }
}
