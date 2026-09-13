package com.dduany.intermediary.proposal;

import com.dduany.intermediary.proposal.dto.ProposalRequest;
import com.dduany.intermediary.proposal.dto.ProposalResponse;
import com.dduany.intermediary.proposal.dto.ProposalStatusRequest;
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
@RequestMapping("/proposals")
@Tag(name = "Proposals", description = "Change sets from agents, waiting for a person to review them in the app")
@SecurityRequirement(name = "bearerAuth")
public class ProposalController {

    private final ProposalService service;

    public ProposalController(ProposalService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List proposals, newest first; filter with ?status=PENDING")
    public List<ProposalResponse> findAll(@RequestParam(required = false) ProposalStatus status) {
        return service.findAll(status);
    }

    @GetMapping("/{id}")
    public ProposalResponse findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Submit a change set for review. Nothing is applied until a person accepts it in the app.")
    public ProposalResponse create(@Valid @RequestBody ProposalRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}/status")
    public ProposalResponse setStatus(@PathVariable Long id, @Valid @RequestBody ProposalStatusRequest request) {
        return service.setStatus(id, request.getStatus());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
