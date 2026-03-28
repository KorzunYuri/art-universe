package yurykorzun.art.universe.music.data.master.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.proposal.AnalysisRequestDto;
import yurykorzun.art.universe.music.data.master.dto.proposal.ProposalDto;
import yurykorzun.art.universe.music.data.master.dto.proposal.ProposalStatsDto;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.List;

public interface ProposalService {

    Page<ProposalDto> findProposals(
        ProposalResolution resolution,
        ProposalType proposalType,
        AttributeTargetType subjectType,
        Long subjectId,
        Pageable pageable
    );

    ProposalDto getById(Long id);

    Page<AnalysisRequestDto> findRequests(Pageable pageable);

    int approveProposals(List<Long> ids, String resolvedBy);

    int declineProposals(List<Long> ids, String resolvedBy);

    ProposalStatsDto getStats();
}
