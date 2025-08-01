package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.EntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for artist-related entity lookup operations
 */
@Slf4j
public class ArtistRelatedLookupService extends AbstractLookupService<ArtistRelatedLookupRequestDTO> {

    public ArtistRelatedLookupService(EntityManager entityManager, EntityType entityType) {
        super(entityManager, entityType);
    }

    @Override
    protected boolean isValidSearchRequest(ArtistRelatedLookupRequestDTO request) {
        // empty name is applicable if artist is provided
        boolean isValid =
            (request.getSearch() != null && !request.getSearch().trim().isEmpty())
            || (request.getMasterArtistId() != null)
            || (request.getDataSource() != null && request.getExternalArtistId() != null);
        if (!isValid) {
            log.error("Invalid search request: {}, masterArtistId: {}, dataSource: {}, externalArtistId: {}",
                request.getSearch(), request.getMasterArtistId(), request.getDataSource(), request.getExternalArtistId());
        }
        return isValid;
    }

    @Override
    protected SqlQueryBuilder.QueryData buildQuery(EntityMetadata metadata, ArtistRelatedLookupRequestDTO request, int limit) {
        SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder();
        sqlQueryBuilder.append(String.format(
            """
            SELECT
                e.id,
                e.name,
                a.id    AS artist_id,
                a.name  AS artist_name
            FROM
                %s e
            JOIN
                artist a
                    ON e.primary_artist_id = a.id
            WHERE
                    (
                            ?1 IS NULL
                        OR  ?1 = ''
                        OR  LOWER(e.name) LIKE LOWER(CONCAT('%%', ?1, '%%'))
                    )
                AND
                    (
                        (
                            CAST(?2 as BIGINT) IS NOT NULL 
                            AND e.primary_artist_id = CAST(?2 as BIGINT)
                        )
                        OR
                        (
                            CAST(?2 as BIGINT) IS NULL
                            AND CAST(?3 as BIGINT) IS NOT NULL
                            AND CAST(?4 as BIGINT) IS NOT NULL
                            AND EXISTS (
                                SELECT 1
                                FROM artist_binding ab
                                WHERE   ab.data_source_id   = CAST(?3 as BIGINT)
                                    AND ab.external_id      = CAST(?4 as BIGINT)
                                    AND ab.master_id        = e.primary_artist_id
                                )
                        )
                        OR
                        (
                            CAST(?2 as BIGINT) IS NULL
                            AND CAST(?3 as BIGINT) IS NULL
                            AND CAST(?4 as BIGINT) IS NULL
                        )
                    )
            ORDER BY
                CASE
                    WHEN    CAST(?2 as BIGINT) IS NOT NULL
                        AND e.primary_artist_id = CAST(?2 as BIGINT)    THEN 0
                                                                        ELSE 1
                END,
                e.name ASC
            LIMIT ?5
            """,
            metadata.getTableName()
        ));

        // set parameters
        sqlQueryBuilder.param(1, request.getSearch() != null ? request.getSearch().trim() : "");
        sqlQueryBuilder.param(2, request.getMasterArtistId());

        // Set data source and external artist ID parameters
        if (request.getExternalArtistId() != null) {
            sqlQueryBuilder.param(3, request.getDataSource().getCode());
            sqlQueryBuilder.param(4, request.getExternalArtistId());
        } else {
            sqlQueryBuilder.param(3, null);
            sqlQueryBuilder.param(4, null);
        }

        sqlQueryBuilder.param(5, limit);

        return sqlQueryBuilder.build();
    }

    @Override
    protected List<LookupResultDTO> mapResultsToDto(List<Object[]> results) {
        return results.stream()
            .map(row -> {
                Long id = ((Number) row[0]).longValue();
                String name = (String) row[1];
                String artistName = (String) row[3];

                return LookupResultDTO.builder()
                    .id(id)
                    .name(artistName + " - " + name)
                    .build();
            })
            .collect(Collectors.toList());
    }

    @Override
    protected ArtistRelatedLookupRequestDTO prepareRequest(ArtistRelatedLookupRequestDTO request, int defaultLimit) {
        return ArtistRelatedLookupRequestDTO.builder()
            .search(request.getSearch())
            .dataSource(request.getDataSource())
            .masterArtistId(request.getMasterArtistId())
            .externalArtistId(request.getExternalArtistId())
            .limit(request.getLimit() != null ? request.getLimit() : defaultLimit)
            .build();
    }
}
