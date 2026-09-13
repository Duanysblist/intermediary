package com.dduany.intermediary.proposal;

import com.dduany.intermediary.ai.dto.ChangeSet;
import com.dduany.intermediary.exception.ResourceNotFoundException;
import com.dduany.intermediary.proposal.dto.ProposalRequest;
import com.dduany.intermediary.proposal.dto.ProposalResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProposalService {

    private static final TypeReference<List<ChangeSet.Change>> CHANGES = new TypeReference<>() {};

    private final ProposalRepository repository;
    private final ObjectMapper json;

    public ProposalService(ProposalRepository repository, ObjectMapper json) {
        this.repository = repository;
        this.json = json;
    }

    @Transactional
    public ProposalResponse create(ProposalRequest request) {
        Proposal proposal = Proposal.builder()
                .source(request.getSource())
                .summary(request.getSummary())
                .changesJson(write(request.getChanges()))
                .status(ProposalStatus.PENDING)
                .build();
        return toResponse(repository.save(proposal));
    }

    @Transactional(readOnly = true)
    public List<ProposalResponse> findAll(ProposalStatus status) {
        List<Proposal> rows = status == null ? repository.findAllByOrderByCreatedAtDesc() : repository.findByStatusOrderByCreatedAtDesc(status);
        return rows.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProposalResponse findById(Long id) {
        return toResponse(get(id));
    }

    @Transactional
    public ProposalResponse setStatus(Long id, ProposalStatus status) {
        Proposal proposal = get(id);
        proposal.setStatus(status);
        return toResponse(repository.saveAndFlush(proposal));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Proposal not found with id: " + id);
        repository.deleteById(id);
    }

    private Proposal get(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Proposal not found with id: " + id));
    }

    private ProposalResponse toResponse(Proposal p) {
        return new ProposalResponse(p.getId(), p.getSource(), p.getSummary(), read(p.getChangesJson()), p.getStatus(), p.getCreatedAt(), p.getUpdatedAt());
    }

    private String write(List<ChangeSet.Change> changes) {
        try {
            return json.writeValueAsString(changes);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Changes could not be serialised", e);
        }
    }

    private List<ChangeSet.Change> read(String text) {
        try {
            return json.readValue(text, CHANGES);
        } catch (JsonProcessingException e) {
            return List.of();
        }
    }
}
