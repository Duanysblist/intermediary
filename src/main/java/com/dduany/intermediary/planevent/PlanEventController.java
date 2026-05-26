package com.dduany.intermediary.planevent;

import com.dduany.intermediary.planevent.dto.PlanEventRequest;
import com.dduany.intermediary.planevent.dto.PlanEventResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/plan-events")
public class PlanEventController {

    private final PlanEventService service;

    public PlanEventController(PlanEventService service) {
        this.service = service;
    }

    @GetMapping
    public List<PlanEventResponse> findAll(@RequestParam(required = false) Long planItemId) {
        if (planItemId != null) {
            return service.findByPlanItemId(planItemId);
        }
        return service.findAll();
    }

    @GetMapping("/{id}")
    public PlanEventResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<PlanEventResponse> create(@Valid @RequestBody PlanEventRequest request) {
        PlanEventResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}