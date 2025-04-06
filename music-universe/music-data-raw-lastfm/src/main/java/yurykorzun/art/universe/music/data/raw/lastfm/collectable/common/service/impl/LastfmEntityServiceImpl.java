package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.data.raw.entity.BaseCollectableEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class LastfmEntityServiceImpl implements LastfmEntityService {

    private final EntityManager entityManager;

    public LastfmEntityServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <E extends BaseCollectableEntity> List<E> findAllUnprocessed(LastfmEntityType entityType, LastfmApiCallType apiCallType) {
        return findAllUnprocessed(entityType, apiCallType, LastfmEntityQueryConfig.builder().build());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends BaseCollectableEntity> List<E> findAllUnprocessed(
        LastfmEntityType entityType, LastfmApiCallType apiCallType, LastfmEntityQueryConfig config
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = (CriteriaQuery<E>) cb.createQuery(entityType.getEntityClass());
        Root<E> entityRoot = (Root<E>) query.from(entityType.getEntityClass());

        // subquery for api_call
        Subquery<Long> idsWithPendingCallsQuery = query.subquery(Long.class);
        Root<LastfmApiCall> apiCallRoot = idsWithPendingCallsQuery.from(LastfmApiCall.class);
        // reused paths
        Path<Object>    entityId = entityRoot.get("id");
        Path<Object>    entityApprovalStatus = entityRoot.get("approvalStatus");
        Path<Long>      apiCallEntityIdRef = apiCallRoot.get("entityId");
        Path<Object>    apiCallTypeField = apiCallRoot.get("type");
        Path<Object>    apiCallEntityType = apiCallRoot.get("entityType");
        Path<Timestamp> apiCallDueDttm = apiCallRoot.get("dueDttm");
        // predicates
        Predicate entityIsApproved = cb.equal(entityApprovalStatus, ApprovalStatus.APPROVED.getCode());
        Predicate apiCallIsOfRequestedType = cb.equal(apiCallTypeField, apiCallType.getCode());
        Predicate apiCallIsForRequestedEntityType = cb.equal(apiCallEntityType, entityType.getCode());
        Predicate apiCallReferencesEntity = cb.equal(apiCallEntityIdRef, entityId);
        Predicate apiCallIsNotExpired = cb.greaterThan(apiCallDueDttm, cb.currentTimestamp());
        // build subquery
        idsWithPendingCallsQuery.select(apiCallEntityIdRef)
                .where(
                        entityIsApproved,
                        apiCallIsOfRequestedType,
                        apiCallIsForRequestedEntityType,
                        apiCallReferencesEntity,
                        apiCallIsNotExpired
                );
        // build sorting based on ROOT entity
        List<Order> jpaOrders = new ArrayList<>();
        for (Sort.Order order : config.getSort()) {
            Path<?> path = entityRoot.get(order.getProperty());
            Order jpaOrder = order.isAscending() ? cb.asc(path) : cb.desc(path);
            jpaOrders.add(jpaOrder);
        }

        // finalize query
        query.select(entityRoot)
            .where(
                cb.equal(entityApprovalStatus, ApprovalStatus.APPROVED.getCode()),
                cb.not(cb.exists(idsWithPendingCallsQuery))
            )
            .orderBy(jpaOrders);

        TypedQuery<E> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(config.getLimit().max());
        return typedQuery.getResultList();
    }

}
