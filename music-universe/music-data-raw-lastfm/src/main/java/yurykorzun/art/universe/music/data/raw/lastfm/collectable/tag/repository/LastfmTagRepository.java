package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.Collection;
import java.util.List;

@Repository
public interface LastfmTagRepository extends JpaRepository<LastfmTag, Long>, LastfmTagRepositoryCustom {

    List<LastfmTag> findAllByNameIn(Collection<String> names);

    List<LastfmTag> findAllByUrlIn(List<String> strings);

    @Query(value = """
        SELECT  t
        FROM    tag t
        WHERE   1=1
            AND ((LOWER(t.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
        """)
    Page<LastfmTag> findTagsWithoutApprovalStatus(
        @Nullable @Param("search") String search,
        Pageable pageable);

    @Query(value = """
        SELECT  t
        FROM    tag t
        WHERE   1=1
            AND ((LOWER(t.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND t.approvalStatus IN (:approvalStatuses)
        """)
    Page<LastfmTag> findTagsWithApprovalStatus(
        @Nullable @Param("search") String search,
        @Param("approvalStatuses") List<ApprovalStatus> approvalStatuses,
        Pageable pageable);

    /**
     * A wrapper for findTags for correct collection parameters resolution.
     * This implementation avoids Hibernate bugs with:
     * 1. Null String parameters being recognized as bytea
     * 2. Empty collections handling
     * 3. Ensures null values are sorted last for numeric fields
     */
    default Page<LastfmTag> findTags(
        String search,
        List<ApprovalStatus> approvalStatuses,
        Pageable pageable
    ) {
        if (approvalStatuses == null || approvalStatuses.isEmpty()) {
            return findTagsWithoutApprovalStatus(search, pageable);
        } else {
            return findTagsWithApprovalStatus(search, approvalStatuses, pageable);
        }
    }
}
