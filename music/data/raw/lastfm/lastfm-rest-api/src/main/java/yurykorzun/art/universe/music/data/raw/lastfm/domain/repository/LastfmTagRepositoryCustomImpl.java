package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.EntityTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmAlbumTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmTrackTag;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class LastfmTagRepositoryCustomImpl implements LastfmTagRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;
    
    // Cache for storing table names and field names
    private final Map<LastfmEntityType, String> relationTableNames = new EnumMap<>(LastfmEntityType.class);
    private final Map<LastfmEntityType, String> entityFieldNames = new EnumMap<>(LastfmEntityType.class);
    private final Map<LastfmEntityType, Class<?>> entityClasses = new EnumMap<>(LastfmEntityType.class);
    
    @PostConstruct
    public void init() {
        // Initialize table name cache when bean is created
        initRelationInfo(LastfmEntityType.ARTIST, LastfmArtistTag.class, "artist", LastfmArtist.class);
        initRelationInfo(LastfmEntityType.ALBUM, LastfmAlbumTag.class, "album", LastfmAlbum.class);
        initRelationInfo(LastfmEntityType.TRACK, LastfmTrackTag.class, "track", LastfmTrack.class);
    }
    
    private void initRelationInfo(LastfmEntityType entityType, Class<?> relationClass, String entityFieldName, Class<?> entityClass) {
        try {
            // Extract relation table name from @Entity annotation
            Entity entityAnnotation = relationClass.getAnnotation(Entity.class);
            if (entityAnnotation != null && !entityAnnotation.name().isEmpty()) {
                relationTableNames.put(entityType, entityAnnotation.name());
            } else {
                // If annotation is not specified or name is empty, use class name
                relationTableNames.put(entityType, relationClass.getSimpleName().toLowerCase());
            }
            
            // Store entity field name and class
            entityFieldNames.put(entityType, entityFieldName);
            entityClasses.put(entityType, entityClass);
            
            log.debug("Initialized relation table name for {}: {}", entityType, relationTableNames.get(entityType));
            log.debug("Initialized entity field name for {}: {}", entityType, entityFieldNames.get(entityType));
        } catch (Exception e) {
            log.error("Failed to initialize relation info for entity type {}: {}", entityType, e.getMessage(), e);
            // Use default values in case of error
            relationTableNames.put(entityType, entityType.name().toLowerCase() + "_tag");
            entityFieldNames.put(entityType, entityType.name().toLowerCase());
        }
    }

    @Override
    public List<EntityTagDto> findTagsByEntityWithFilters(
        LastfmEntityType entityType, 
        Long entityId,
        Integer minUsageCount,
        List<ApprovalStatus> approvalStatuses,
        Pageable pageable
    ) {
        // Get relation table name and entity field name from cache
        String relationTableName = relationTableNames.get(entityType);
        String entityFieldName = entityFieldNames.get(entityType);
        
        if (relationTableName == null || entityFieldName == null) {
            throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        }
        
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT t, rel.approvalStatus, e.approvalStatus, COALESCE(rel.usageCount, 0) FROM tag t ");
        queryBuilder.append("JOIN ").append(relationTableName).append(" rel ");
        queryBuilder.append("ON t.id = rel.tag.id ");
        queryBuilder.append("JOIN rel.").append(entityFieldName).append(" e ");
        queryBuilder.append("WHERE e.id = :entityId ");
        
        // Add minUsageCount filter if provided
        if (minUsageCount != null && minUsageCount > 0) {
            queryBuilder.append("AND rel.usageCount >= :minUsageCount ");
        }
        
        // Add approvalStatus filter if provided
        if (!CollectionUtils.isEmpty(approvalStatuses)) {
            queryBuilder.append("AND t.approvalStatus IN :approvalStatuses ");
        }
        
        // Add sorting
        if (pageable != null && pageable.getSort().isSorted()) {
            queryBuilder.append("ORDER BY ");
            boolean first = true;
            
            for (Sort.Order order : pageable.getSort()) {
                if (!first) {
                    queryBuilder.append(", ");
                }
                
                String property = order.getProperty();
                // Determine if property belongs to tag, relation, or entity
                String sortField = getSortField(property);
                
                queryBuilder.append(sortField).append(" ").append(order.getDirection().name());
                first = false;
            }
        } else {
            // Default sort by name
            queryBuilder.append("ORDER BY t.name ASC");
        }
        
        log.debug("Executing query: {}", queryBuilder);
        
        // Create and execute query
        Query query = entityManager.createQuery(queryBuilder.toString());
        query.setParameter("entityId", entityId);
        
        // Set optional parameters if provided
        if (minUsageCount != null && minUsageCount > 0) {
            query.setParameter("minUsageCount", minUsageCount);
        }
        
        if (!CollectionUtils.isEmpty(approvalStatuses)) {
            query.setParameter("approvalStatuses", approvalStatuses);
        }
        
        // Apply pagination if provided
        if (pageable != null) {
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());
        }
        
        @SuppressWarnings("unchecked")
        List<Object[]> resultList = query.getResultList();
        
        // Convert result to EntityTagDto objects directly
        List<EntityTagDto> result = new ArrayList<>(resultList.size());
        for (Object[] row : resultList) {
            LastfmTag tag = (LastfmTag) row[0];
            ApprovalStatus relationApprovalStatus = (ApprovalStatus) row[1];
            ApprovalStatus entityApprovalStatus = (ApprovalStatus) row[2];
            Integer usageCount = (Integer) row[3];
            
            result.add(new EntityTagDto(
                tag.getId(),
                tag.getName(),
                relationApprovalStatus.getCode(),
                tag.getApprovalStatus().getCode(),
                entityApprovalStatus.getCode(),
                usageCount
            ));
        }
        
        return result;
    }
    
    private String getSortField(String property) {
        // List of properties that belong to tag entity
        List<String> tagProperties = List.of("id", "name", "url", "approvalStatus");
        
        if (tagProperties.contains(property)) {
            return "t." + property;
        } else if ("usageCount".equals(property)) {
            return "rel.usageCount";
        } else if ("entityApprovalStatus".equals(property)) {
            return "e.approvalStatus";
        } else if ("tagApprovalStatus".equals(property)) {
            return "t.approvalStatus";
        } else {
            // Default to tag name if unknown property
            return "t.name";
        }
    }
}
