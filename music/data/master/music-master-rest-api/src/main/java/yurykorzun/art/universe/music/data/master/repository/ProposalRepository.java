package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.entity.proposal.Proposal;
import yurykorzun.art.universe.music.data.semantic.model.ProposalResolution;
import yurykorzun.art.universe.music.data.semantic.model.ProposalType;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProposalRepository extends JpaRepository<Proposal, Long> {

    @Query("""
        SELECT p FROM Proposal p
        WHERE (:resolution IS NULL OR p.resolution = :resolution)
          AND (:proposalType IS NULL OR p.proposalType = :proposalType)
          AND (:subjectType IS NULL OR p.subjectType = :subjectType)
          AND (:subjectId IS NULL OR p.subjectId = :subjectId)
        ORDER BY p.createdAt DESC
        """)
    Page<Proposal> findFiltered(
        @Param("resolution") ProposalResolution resolution,
        @Param("proposalType") ProposalType proposalType,
        @Param("subjectType") AttributeTargetType subjectType,
        @Param("subjectId") Long subjectId,
        Pageable pageable
    );

    List<Proposal> findByRequestId(UUID requestId);

    @Modifying
    @Query("""
        UPDATE Proposal p
        SET p.resolution = :resolution,
            p.resolvedBy = :resolvedBy,
            p.resolvedAt = CURRENT_TIMESTAMP
        WHERE p.id IN :ids AND p.resolution = :pendingResolution
        """)
    int resolveByIds(
        @Param("ids") List<Long> ids,
        @Param("resolution") ProposalResolution resolution,
        @Param("resolvedBy") String resolvedBy,
        @Param("pendingResolution") ProposalResolution pendingResolution
    );
}
