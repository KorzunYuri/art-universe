package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository.LastfmEntityRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;

import java.util.List;

@Service
public class LastfmEntityRelationServiceImpl implements LastfmEntityRelationService {

    private final LastfmEntityRelationRepository entityRelationRepository;
    private final JdbcTemplate jdbcTemplate;

    public LastfmEntityRelationServiceImpl(LastfmEntityRelationRepository entityRelationRepository, JdbcTemplate jdbcTemplate) {
        this.entityRelationRepository = entityRelationRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void upsertEntityRelation(LastfmEntityRelation entityRelation) {
        entityRelationRepository.upsertEntityRelation(entityRelation);
    }

    @Override
    @Transactional
    public void upsertEntityRelations(List<LastfmEntityRelation> entities) {
        // replace named parameters for jdbcTemplate
        String sql = LastfmEntityRelationRepository.ENTITY_RELATION_UPSERT_SQL.replaceAll(":[a-zA-Z]*", "?");

        // make jdbc parameters batch
        List<Object[]> batchArgs = entities.stream()
                .map(entityRelationRepository.entityToUpsertSqlParamsMapper())
            .toList();

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }
}
