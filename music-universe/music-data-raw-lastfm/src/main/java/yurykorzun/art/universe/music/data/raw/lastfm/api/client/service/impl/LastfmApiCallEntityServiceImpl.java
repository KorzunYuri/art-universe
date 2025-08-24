package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.BlacklistedEntityUrl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
public class LastfmApiCallEntityServiceImpl implements LastfmApiCallEntityService {

    private final EntityManager entityManager;

    public LastfmApiCallEntityServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public <E extends BaseLastfmEntity> List<E> findAllUnprocessed(LastfmEntityType entityType, LastfmApiCallType apiCallType) {
        return findAllUnprocessed(entityType, apiCallType, LastfmApiCallEntityQueryConfig.builder().build());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E extends BaseLastfmEntity> List<E> findAllUnprocessed(
        LastfmEntityType entityType, LastfmApiCallType apiCallType, LastfmApiCallEntityQueryConfig config
    ) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<E> query = (CriteriaQuery<E>) cb.createQuery(entityType.getEntityClass());
        Root<E> entityRoot = (Root<E>) query.from(entityType.getEntityClass());

        // subquery for api_call
        Subquery<Long> idsWithPendingCallsQuery = query.subquery(Long.class);
        Root<LastfmApiCall> apiCallRoot = idsWithPendingCallsQuery.from(LastfmApiCall.class);

        // reused paths
        Path<Object>    entityId = entityRoot.get("id");
        Path<Long>      apiCallEntityIdRef = apiCallRoot.get("entityId");
        Path<Object>    apiCallTypeField = apiCallRoot.get("type");
        Path<Object>    apiCallEntityType = apiCallRoot.get("entityType");
        Path<Timestamp> apiCallDueDttm = apiCallRoot.get("dueDttm");

        // predicates for API call subquery
        List<Predicate> subQueryPredicates = new ArrayList<>();
        subQueryPredicates.add(cb.equal(apiCallTypeField, apiCallType.getCode()));
        subQueryPredicates.add(cb.equal(apiCallEntityType, entityType.getCode()));
        subQueryPredicates.add(cb.equal(apiCallEntityIdRef, entityId));
        subQueryPredicates.add(cb.greaterThan(apiCallDueDttm, cb.currentTimestamp()));

        // Approval status filter for main query
        Path<Integer> entityApprovalStatus = entityRoot.get("approvalStatus");
        Predicate entityApprovalFilter;
        if (config.isApprovedEntitiesOnly()) {
            entityApprovalFilter = cb.equal(entityApprovalStatus, ApprovalStatus.APPROVED);
        } else {
            CriteriaBuilder.In<Integer> entityApprovalStatusIn = cb.in(entityApprovalStatus);
            entityApprovalStatusIn
                .value(ApprovalStatus.APPROVED.getCode())
                .value(ApprovalStatus.PENDING.getCode());
            entityApprovalFilter = entityApprovalStatusIn;
        }

        // build subquery
        idsWithPendingCallsQuery
            .select(apiCallEntityIdRef)
            .where(subQueryPredicates.toArray(new Predicate[]{}));

        // subquery for blacklisted URLs
        Subquery<String> blacklistedUrlsQuery = query.subquery(String.class);
        Root<BlacklistedEntityUrl> blacklistRoot = blacklistedUrlsQuery.from(BlacklistedEntityUrl.class);
        
        Path<String> entityUrl = entityRoot.get("url");
        Path<String> blacklistUrl = blacklistRoot.get("url");
        Path<Object> blacklistEntityType = blacklistRoot.get("entityType");
        
        blacklistedUrlsQuery
            .select(blacklistUrl)
            .where(
                cb.equal(blacklistEntityType, entityType.getCode()),
                cb.equal(blacklistUrl, entityUrl)
            );

        // build sorting based on ROOT entity
        List<Order> jpaOrders = buildOrders(config, entityRoot, cb);

        // finalize query
        query.select(entityRoot)
            .where(
                entityApprovalFilter,
                cb.not(cb.exists(idsWithPendingCallsQuery)),
                cb.not(cb.exists(blacklistedUrlsQuery))
            )
            .orderBy(jpaOrders);

        TypedQuery<E> typedQuery = entityManager.createQuery(query);
        typedQuery.setMaxResults(config.getLimit().max());
        return typedQuery.getResultList();
    }

    /**
     * Builder ORDER expression based on {@link LastfmApiCallEntityQueryConfig} provided by caller.
     * Number values will be wrapped in Coalesce function to support natural priority.
     */
    private <E extends BaseLastfmEntity> List<Order> buildOrders(
        LastfmApiCallEntityQueryConfig config,
        Root<E> entityRoot,
        CriteriaBuilder cb
    ) {
        List<Order> jpaOrders = new ArrayList<>();
        for (Sort.Order order : config.getSort()) {
            Expression<?> sortExpr;
            Path<?> path = entityRoot.get(order.getProperty());
            Class<?> javaType = path.getJavaType();
            // build coalesce(value, 0) for numbers
            if (Integer.class.isAssignableFrom(javaType)) {
                CriteriaBuilder.Coalesce<Integer> coalesce = cb.coalesce();
                coalesce.value(path.as(Integer.class));
                coalesce.value(0);
                sortExpr = coalesce;
            } else {
                sortExpr = path;
            }
            Order jpaOrder = order.isAscending() ? cb.asc(sortExpr) : cb.desc(sortExpr);
            jpaOrders.add(jpaOrder);
        }
        return jpaOrders;
    }

}
