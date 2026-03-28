package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.proposal.AnalysisRequest;

import java.util.UUID;

@Repository
public interface AnalysisRequestRepository extends JpaRepository<AnalysisRequest, UUID> {

    Page<AnalysisRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
