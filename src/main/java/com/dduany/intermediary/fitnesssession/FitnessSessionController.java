package com.dduany.intermediary.fitnesssession;

import com.dduany.intermediary.fitnesssession.dto.FitnessSessionRequest;
import com.dduany.intermediary.fitnesssession.dto.FitnessSessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/fitness-sessions")
public class FitnessSessionController {

    private final FitnessSessionService service;

    public FitnessSessionController(FitnessSessionService service) {
        this.service = service;
    }

    @GetMapping
    public List<FitnessSessionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public FitnessSessionResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public ResponseEntity<FitnessSessionResponse> create(@Valid @RequestBody FitnessSessionRequest request) {
        FitnessSessionResponse response = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public FitnessSessionResponse update(@PathVariable Long id, @Valid @RequestBody FitnessSessionRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
