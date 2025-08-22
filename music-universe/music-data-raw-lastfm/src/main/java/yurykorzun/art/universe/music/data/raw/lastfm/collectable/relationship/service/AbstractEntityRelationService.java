package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmSameEntityRelation;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;

@Slf4j
public abstract class AbstractEntityRelationService<T extends BaseLastfmEntityRelation<?, ?>>
    implements BaseLastfmEntityRelationService<T>{

    protected final JdbcTemplate jdbcTemplate;
    protected final EntityManager entityManager;
    private EntityRelationMetadata<T> metadata;

    private static final Set<String> IGNORED_INSERT_COLUMNS = Set.of("createdAt", "updatedAt");

    protected AbstractEntityRelationService(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
    }

    @PostConstruct
    protected void initializeMetadata() {
        Class<T> entityClass = getRelationClass();
        this.metadata = buildMetadata(entityClass);
        log.debug("Initialized metadata for {}: table={}, insertColumns={}, conflictColumns={}", 
                entityClass.getSimpleName(), 
                metadata.getTableName(), 
                metadata.getInsertColumns(), 
                metadata.getConflictColumns());
    }

    public void upsertAll(List<T> entities) {
        if (entities.isEmpty()) {
            return;
        }

        String sql = buildUpsertSql(entities.get(0));
        Function<T, Object[]> paramMapper = metadata.getParameterMapper();
        
        List<Object[]> batchArgs = entities.stream()
                .map(paramMapper)
                .toList();

        entityManager.flush(); // flush dependent entities, if any
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    /**
     * Builds the UPSERT SQL statement for the specific entity type.
     * Uses COALESCE logic for fields that shouldn't be overwritten with NULL.
     * Only updates coalesceFields and updated_at, and updated_at only changes if any coalesceField actually changes.
     */
    protected String buildUpsertSql(T sampleEntity) {
        String tableName = metadata.getTableName();
        List<String> insertColumns = metadata.getInsertColumns();
        List<String> conflictColumns = metadata.getConflictColumns();
        List<String> coalesceFields = sampleEntity.getUpdatableFields();
        
        StringBuilder sql = new StringBuilder();
        sql.append("INSERT INTO ").append(tableName).append(" (");
        sql.append(String.join(", ", insertColumns));
        sql.append(", created_at, updated_at");
        sql.append(") VALUES (");
        sql.append("?, ".repeat(insertColumns.size() - 1)).append("?");
        sql.append(", NOW(), NOW()"); // created_at, updated_at
        sql.append(") ON CONFLICT (");
        sql.append(String.join(", ", conflictColumns));
        sql.append(")");
        // for non-changed entities - don't update
        if (coalesceFields.isEmpty()) {
            sql.append(" DO NOTHING ");
            return sql.toString();
        }

        // Build UPDATE SET clause with COALESCE logic - only for coalesceFields
        sql.append(" DO UPDATE SET ");
        List<String> updateClauses = coalesceFields.stream()
                .map(col -> col + " = COALESCE(EXCLUDED." + col + ", " + tableName + "." + col + ")")
                .toList();
        sql.append(String.join(", ", updateClauses));
        
        // Only update updated_at if any coalesceField actually changed
        if (!coalesceFields.isEmpty()) {
            sql.append(", updated_at = CASE WHEN ");
            
            // Build condition to check if any coalesceField changed
            List<String> changeConditions = coalesceFields.stream()
                    .map(col -> String.format("(COALESCE(EXCLUDED.%s, %s.%s) IS DISTINCT FROM %s.%s)", 
                            col, tableName, col, tableName, col))
                    .toList();
            
            sql.append(String.join(" OR ", changeConditions));
            sql.append(" THEN NOW() ELSE ").append(tableName).append(".updated_at END");
        }
        
        return sql.toString();
    }

    /**
     * Builds metadata for the entity relation
     */
    private EntityRelationMetadata<T> buildMetadata(Class<T> entityClass) {
        String tableName = extractTableName(entityClass);
        List<String> insertColumns = extractInsertColumns(entityClass);
        List<String> conflictColumns = extractConflictColumns(entityClass);
        Function<T, Object[]> parameterMapper = buildParameterMapper(entityClass, insertColumns);

        return EntityRelationMetadata.<T>builder()
                .tableName(tableName)
                .insertColumns(insertColumns)
                .conflictColumns(conflictColumns)
                .parameterMapper(parameterMapper)
                .build();
    }

    /**
     * Extracts table name from @Entity annotation
     */
    private String extractTableName(Class<T> entityClass) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new IllegalStateException("Entity class must have @Entity annotation: " + entityClass.getName());
        }
        return entityAnnotation.name();
    }

    /**
     * Extracts all columns except id from entity fields
     */
    private List<String> extractInsertColumns(Class<T> entityClass) {
        List<String> columns = new ArrayList<>();
        
        // Get all fields from the class hierarchy
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                // Skip id field
                if (field.isAnnotationPresent(Id.class)) {
                    continue;
                }

                // Skip technical fields
                if (IGNORED_INSERT_COLUMNS.contains(field.getName())) {
                    continue;
                }
                
                // Add fields with @Column annotation
                if (field.isAnnotationPresent(Column.class)) {
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    String columnName = columnAnnotation.name().isEmpty() ? 
                            convertFieldNameToColumnName(field.getName()) : columnAnnotation.name();
                    columns.add(columnName);
                }
                
                // Add fields with @JoinColumn annotation
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
                    columns.add(joinColumnAnnotation.name());
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        
        return columns;
    }

    /**
     * Extracts conflict columns based on entity relationship pattern
     */
    private List<String> extractConflictColumns(Class<T> relationClass) {
        List<String> conflictColumns = new ArrayList<>();
        
        // Get source and target entity types
        RelationEntityTypes entityTypes = getEntityTypes(relationClass);
        
        String sourceEntityName = getEntityName(entityTypes.sourceClass());
        String targetEntityName = getEntityName(entityTypes.targetClass());
        
        if (entityTypes.sourceClass().equals(entityTypes.targetClass())) {
            // Same entity type - use source_[entity]_id, target_[entity]_id pattern
            conflictColumns.add("source_" + sourceEntityName + "_id");
            conflictColumns.add("target_" + targetEntityName + "_id");
        } else {
            // Different entity types - use [entity]_id pattern
            conflictColumns.add(sourceEntityName + "_id");
            conflictColumns.add(targetEntityName + "_id");
        }
        
        // Check for relation_type field
        if (hasRelationTypeField(relationClass)) {
            conflictColumns.add("relation_type");
        }
        
        return conflictColumns;
    }

    /**
     * Gets the entity class from generic type parameter
     */
    @SuppressWarnings("unchecked")
    private Class<T> getRelationClass() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            return (Class<T>) parameterizedType.getActualTypeArguments()[0];
        }
        throw new IllegalStateException("Cannot determine entity class for " + getClass().getName());
    }

    private RelationEntityTypes getEntityTypes(Class<T> relationClass) {

        boolean isSameEntityRelation = isSubclassOf(relationClass, BaseLastfmSameEntityRelation.class.getSimpleName());
        // For same entity relations, get the single type parameter
        // For different entity relations, get from BaseLastfmEntityRelation<S, T>
        Class<?> sourceType = getSourceEntityType(relationClass);
        Class<?> targetType = isSameEntityRelation ? sourceType : getTargetEntityType(relationClass);
        return new RelationEntityTypes(sourceType, targetType);
    }

    /**
     * Gets the source entity type from the entity relation generic parameters
     */
    private Class<?> getSourceEntityType(Class<T> relationClass) {
        Type genericSuperclass = relationClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length < 1) {
                throw new IllegalStateException(
                    String.format("Argument types count mismatch for class %s: expected at least 1, got: %s",
                        relationClass.getName(), typeArguments.length));
            }
            return (Class<?>) typeArguments[0];
        }
        throw new IllegalStateException("Cannot determine target entity type for " + relationClass.getName());
    }

    /**
     * Gets the target entity type from the entity relation generic parameters
     */
    private Class<?> getTargetEntityType(Class<T> relationClass) {
        Type genericSuperclass = relationClass.getGenericSuperclass();
        if (genericSuperclass instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length < 2) {
                throw new IllegalStateException(
                    String.format("Argument types count mismatch for class %s: expected at least 2, got: %s",
                        relationClass.getName(), typeArguments.length));
            }
            return (Class<?>) typeArguments[1];
        }
        throw new IllegalStateException("Cannot determine target entity type for " + relationClass.getName());
    }

    /**
     * Checks if entity has relation_type field
     */
    private boolean hasRelationTypeField(Class<T> entityClass) {
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Column.class)) {
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    if ("relation_type".equals(columnAnnotation.name())) {
                        return true;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    /**
     * Gets entity name from @Entity annotation
     */
    private String getEntityName(Class<?> entityClass) {
        Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
        if (entityAnnotation == null) {
            throw new IllegalStateException("Entity class must have @Entity annotation: " + entityClass.getName());
        }
        return entityAnnotation.name();
    }

    /**
     * Builds parameter mapper function for the entity that maps field values to parameters
     * in the exact order specified by insertColumns
     */
    private Function<T, Object[]> buildParameterMapper(Class<T> entityClass, List<String> insertColumns) {
        // Build a map of column name to field for efficient lookup
        Map<String, Field> columnToFieldMap = buildColumnToFieldMap(entityClass);
        
        return entity -> {
            Object[] values = new Object[insertColumns.size()];
            
            for (int i = 0; i < insertColumns.size(); i++) {
                String columnName = insertColumns.get(i);
                Field field = columnToFieldMap.get(columnName);
                
                if (field == null) {
                    throw new IllegalStateException("No field found for column: " + columnName);
                }
                
                try {
                    field.setAccessible(true);
                    Object value = field.get(entity);
                    
                    // Handle different field types
                    if (field.isAnnotationPresent(JoinColumn.class) && value != null) {
                        // Extract ID from referenced entity
                        Field idField = findIdField(value.getClass());
                        if (idField != null) {
                            idField.setAccessible(true);
                            value = idField.get(value);
                        }
                    } else if (field.isAnnotationPresent(Convert.class) && value != null) {
                        // Handle fields with converters (like ApprovalStatus)
                        Convert convertAnnotation = field.getAnnotation(Convert.class);
                        Class<?> converterClass = convertAnnotation.converter();
                        
                        try {
                            // Create converter instance and convert to database column
                            Object converter = converterClass.getDeclaredConstructor().newInstance();
                            if (converter instanceof AttributeConverter) {
                                @SuppressWarnings("unchecked")
                                AttributeConverter<Object, Object> attributeConverter = 
                                    (AttributeConverter<Object, Object>) converter;
                                value = attributeConverter.convertToDatabaseColumn(value);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to convert field value using converter: " + 
                                                     converterClass.getName(), e);
                        }
                    }
                    
                    values[i] = value;
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access field: " + field.getName(), e);
                }
            }
            
            return values;
        };
    }
    
    /**
     * Builds a map of column names to their corresponding fields from the entity class hierarchy
     */
    private Map<String, Field> buildColumnToFieldMap(Class<T> entityClass) {
        Map<String, Field> columnToFieldMap = new HashMap<>();
        
        // Get all fields from the class hierarchy
        Class<?> currentClass = entityClass;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                // Skip id field
                if (field.isAnnotationPresent(Id.class)) {
                    continue;
                }
                
                String columnName = null;
                
                // Handle @Column annotation
                if (field.isAnnotationPresent(Column.class)) {
                    Column columnAnnotation = field.getAnnotation(Column.class);
                    columnName = columnAnnotation.name().isEmpty() ? 
                            convertFieldNameToColumnName(field.getName()) : columnAnnotation.name();
                }
                
                // Handle @JoinColumn annotation
                if (field.isAnnotationPresent(JoinColumn.class)) {
                    JoinColumn joinColumnAnnotation = field.getAnnotation(JoinColumn.class);
                    columnName = joinColumnAnnotation.name();
                }
                
                if (columnName != null) {
                    columnToFieldMap.put(columnName, field);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        
        return columnToFieldMap;
    }

    /**
     * Finds the @Id field in a class
     */
    private Field findIdField(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (field.isAnnotationPresent(Id.class)) {
                    return field;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    /**
     * Converts camelCase field name to snake_case column name
     */
    private String convertFieldNameToColumnName(String fieldName) {
        return fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * Checks if a class is a subclass of the specified class name
     */
    private boolean isSubclassOf(Class<?> clazz, String superclassSimpleName) {
        Class<?> currentClass = clazz;
        while (currentClass != null && currentClass != Object.class) {
            if (currentClass.getSimpleName().equals(superclassSimpleName)) {
                return true;
            }
            currentClass = currentClass.getSuperclass();
        }
        return false;
    }

    private record RelationEntityTypes(
        Class<?> sourceClass,
        Class<?> targetClass
    ) {
    }
}
