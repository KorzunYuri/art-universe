package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.proposal.AnalysisRequestDto;
import yurykorzun.art.universe.music.data.master.dto.proposal.ProposalDto;
import yurykorzun.art.universe.music.data.master.dto.proposal.ProposalResolveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.proposal.ProposalStatsDto;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.service.ProposalService;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

@RestController
@RequestMapping("/api/v1/proposals")
public class ProposalController {

    private final ProposalService proposalService;

    public ProposalController(ProposalService proposalService) {
        this.proposalService = proposalService;
    }

    @GetMapping
    public Page<ProposalDto> findProposals(
        @RequestParam(required = false) ProposalResolution resolution,
        @RequestParam(required = false) ProposalType proposalType,
        @RequestParam(required = false) AttributeTargetType subjectType,
        @RequestParam(required = false) Long subjectId,
        Pageable pageable
    ) {
        return proposalService.findProposals(resolution, proposalType, subjectType, subjectId, pageable);
    }

    @GetMapping("/{id}")
    public ProposalDto getById(@PathVariable Long id) {
        return proposalService.getById(id);
    }

    @GetMapping("/requests")
    public Page<AnalysisRequestDto> findRequests(Pageable pageable) {
        return proposalService.findRequests(pageable);
    }

    @PostMapping("/approve")
    public int approve(@Valid @RequestBody ProposalResolveRequestDTO request) {
        return proposalService.approveProposals(request.getIds(), "manual");
    }

    @PostMapping("/decline")
    public int decline(@Valid @RequestBody ProposalResolveRequestDTO request) {
        return proposalService.declineProposals(request.getIds(), "manual");
    }

    @GetMapping("/stats")
    public ProposalStatsDto getStats() {
        return proposalService.getStats();
    }
}
