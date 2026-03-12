package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common.BaseSpotifyEntity;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.sql.Timestamp;
import java.util.List;

@Service
public class SpotifyApiCallEntityServiceImpl implements SpotifyApiCallEntityService {

    private static final int DEFAULT_LIMIT = 500;

    private final EntityManager entityManager;

    public SpotifyApiCallEntityServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends BaseSpotifyEntity> List<E> findAllWithoutActiveCalls(
        Class<E> entityClass, SpotifyApiCallType callType
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = cb.createQuery(entityClass);
        Root<E> entityRoot = query.from(entityClass);

        // subquery: api_calls of the given type that are active (not expired) for this entity's spotifyId
        Subquery<String> activeCallsSubquery = query.subquery(String.class);
        Root<SpotifyApiCall> apiCallRoot = activeCallsSubquery.from(SpotifyApiCall.class);

        Path<String> entitySpotifyId = entityRoot.get("spotifyId");
        Path<String> callSpotifyId = apiCallRoot.get("spotifyId");
        Path<Object> callTypeField = apiCallRoot.get("type");
        @SuppressWarnings("unchecked")
        Path<Timestamp> callDueDttm = (Path<Timestamp>) (Path<?>) apiCallRoot.get("dueDttm");

        activeCallsSubquery
            .select(callSpotifyId)
            .where(
                cb.equal(callTypeField, callType.getCode()),
                cb.equal(callSpotifyId, entitySpotifyId),
                cb.greaterThan(callDueDttm, cb.currentTimestamp()),
                cb.isNotNull(callSpotifyId)
            );

        query.select(entityRoot)
            .where(
                cb.isNotNull(entitySpotifyId),
                cb.not(cb.exists(activeCallsSubquery))
            );

        TypedQuery<E> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(DEFAULT_LIMIT);
        return typedQuery.getResultList();
    }
}
