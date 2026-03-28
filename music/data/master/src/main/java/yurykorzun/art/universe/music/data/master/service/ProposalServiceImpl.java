package yurykorzun.art.universe.music.data.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.proposal.AnalysisRequestDto;
import yurykorzun.art.universe.music.data.master.dto.proposal.ProposalDto;
import yurykorzun.art.universe.music.data.master.dto.proposal.ProposalStatsDto;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.entity.proposal.AnalysisRequest;
import yurykorzun.art.universe.music.data.master.entity.proposal.Proposal;
import yurykorzun.art.universe.music.data.master.repository.AnalysisRequestRepository;
import yurykorzun.art.universe.music.data.master.repository.ProposalRepository;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProposalServiceImpl implements ProposalService {

    private final ProposalRepository proposalRepository;
    private final AnalysisRequestRepository analysisRequestRepository;

    public ProposalServiceImpl(
        ProposalRepository proposalRepository,
        AnalysisRequestRepository analysisRequestRepository
    ) {
        this.proposalRepository = proposalRepository;
        this.analysisRequestRepository = analysisRequestRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProposalDto> findProposals(
        ProposalResolution resolution,
        ProposalType proposalType,
        AttributeTargetType subjectType,
        Long subjectId,
        Pageable pageable
    ) {
        return proposalRepository.findFiltered(resolution, proposalType, subjectType, subjectId, pageable)
            .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProposalDto getById(Long id) {
        Proposal proposal = proposalRepository.findById(id)
            .orElseThrow(() -> new CustomEntityNotFoundException("Proposal", id));
        return mapToDto(proposal);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AnalysisRequestDto> findRequests(Pageable pageable) {
        return analysisRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
            .map(this::mapRequestToDto);
    }

    @Override
    @Transactional
    public int approveProposals(List<Long> ids, String resolvedBy) {
        return proposalRepository.resolveByIds(ids, ProposalResolution.APPROVED, resolvedBy, ProposalResolution.PENDING);
    }

    @Override
    @Transactional
    public int declineProposals(List<Long> ids, String resolvedBy) {
        return proposalRepository.resolveByIds(ids, ProposalResolution.DECLINED, resolvedBy, ProposalResolution.PENDING);
    }

    @Override
    @Transactional(readOnly = true)
    public ProposalStatsDto getStats() {
        List<Proposal> all = proposalRepository.findAll();

        Map<String, Long> byResolution = all.stream()
            .collect(Collectors.groupingBy(
                p -> p.getResolution().getName(),
                Collectors.counting()
            ));

        Map<String, Long> byType = all.stream()
            .collect(Collectors.groupingBy(
                p -> p.getProposalType().getName(),
                Collectors.counting()
            ));

        return ProposalStatsDto.builder()
            .totalCount(all.size())
            .byResolution(byResolution)
            .byType(byType)
            .build();
    }

    private ProposalDto mapToDto(Proposal p) {
        return ProposalDto.builder()
            .id(p.getId())
            .requestId(p.getRequestId())
            .synthId(p.getSynthId())
            .proposalType(p.getProposalType())
            .subjectType(p.getSubjectType())
            .subjectId(p.getSubjectId())
            .subjectRef(p.getSubjectRef())
            .confidence(p.getConfidence())
            .reasoning(p.getReasoning())
            .payload(p.getPayload())
            .resolution(p.getResolution())
            .resolvedBy(p.getResolvedBy())
            .resolvedAt(p.getResolvedAt())
            .appliedRef(p.getAppliedRef())
            .createdAt(p.getCreatedAt())
            .build();
    }

    private AnalysisRequestDto mapRequestToDto(AnalysisRequest r) {
        return AnalysisRequestDto.builder()
            .id(r.getId())
            .ticketId(r.getTicketId())
            .analysisVersion(r.getAnalysisVersion())
            .dataSource(r.getDataSource())
            .subjectType(r.getSubjectType())
            .subjectId(r.getSubjectId())
            .status(r.getStatus())
            .llmProvider(r.getLlmProvider())
            .llmModel(r.getLlmModel())
            .promptTokens(r.getPromptTokens())
            .completionTokens(r.getCompletionTokens())
            .errorMessage(r.getErrorMessage())
            .createdAt(r.getCreatedAt())
            .completedAt(r.getCompletedAt())
            .build();
    }
}
