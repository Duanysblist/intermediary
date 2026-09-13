package com.dduany.intermediary.recurringplan;

import com.dduany.intermediary.planitem.dto.PlanItemResponse;
import com.dduany.intermediary.recurringplan.dto.RecurringPlanRequest;
import com.dduany.intermediary.recurringplan.dto.RecurringPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/recurring-plans")
@Tag(name = "Recurring plans", description = "Routines that generate plan items on chosen weekdays")
@SecurityRequirement(name = "bearerAuth")
public class RecurringPlanController {

    private final RecurringPlanService service;

    public RecurringPlanController(RecurringPlanService service) {
        this.service = service;
    }

    @GetMapping
    public List<RecurringPlanResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RecurringPlanResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringPlanResponse create(@Valid @RequestBody RecurringPlanRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public RecurringPlanResponse update(@PathVariable Long id, @Valid @RequestBody RecurringPlanRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/generate")
    @Operation(summary = "Create plan items for every active routine from today through today + days (idempotent)")
    public List<PlanItemResponse> generate(@RequestParam(defaultValue = "14") int days) {
        return service.generate(days);
    }
}
