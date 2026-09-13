package com.dduany.intermediary.proposal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProposalRepository extends JpaRepository<Proposal, Long> {
    List<Proposal> findByStatusOrderByCreatedAtDesc(ProposalStatus status);
    List<Proposal> findAllByOrderByCreatedAtDesc();
}
