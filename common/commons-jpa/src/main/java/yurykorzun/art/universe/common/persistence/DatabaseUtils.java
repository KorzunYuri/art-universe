package yurykorzun.art.universe.common.persistence;

import jakarta.persistence.EntityManager;

/**
 * Database utility methods for safe DDL operations
 */
public class DatabaseUtils {
    
    private DatabaseUtils() {
        // Utility class
    }

    private enum DbObjectType {
        TABLE,
        VIEW
    }

    /**
     * Validate object name
     */
    public static DbObjectMetadata validateObjectName(String objectNameFull) {
        if (objectNameFull == null || objectNameFull.trim().isEmpty()) {
            throw new IllegalArgumentException("DB object name cannot be null or empty");
        }

        String[] parts = objectNameFull.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException(String.format("DB object name must be in format 'schemaName.table', got: '%s'", objectNameFull));
        }
        String schema = parts[0];
        String table = parts[1];

        // Validate schemaName and table names contain only safe characters
        validateIdentifier(schema, "schemaName");
        validateIdentifier(table, "table");

        return new DbObjectMetadata(schema, table);
    }

    /**
     * Validate and extract db object metadata
     * @param objectNameFull object name in format "schemaName.objectName"
     */
    private static DbObjectMetadata parseObject(String objectNameFull) {
        return validateObjectName(objectNameFull);
    }

    private static void dropObject(EntityManager entityManager, String objectNameFull, DbObjectType objectType) {

        DbObjectMetadata dbObj = parseObject(objectNameFull);

        // Use format with %I for safe identifier substitution
        String sql = String.format("DROP %s IF EXISTS %s.%s",
            objectType.name(), quoteIdentifier(dbObj.schemaName()), quoteIdentifier(dbObj.objectName()));

        entityManager.createNativeQuery(sql).executeUpdate();
    }

    /**
     * Safely drops a table with SQL injection protection
     * @param entityManager JPA EntityManager
     * @param tableNameFull Table name in format "schemaName.table"
     */
    public static void dropTable(EntityManager entityManager, String tableNameFull) {
        dropObject(entityManager, tableNameFull, DbObjectType.TABLE);
    }

    /**
     * Safely drops a view with SQL injection protection
     * @param entityManager JPA EntityManager
     * @param viewNameFull Table objectName in format "schemaName.table"
     */
    public static void dropView(EntityManager entityManager, String viewNameFull) {
        dropObject(entityManager, viewNameFull, DbObjectType.VIEW);
    }
    
    /**
     * Checks if a table exists in the database
     * @param entityManager JPA EntityManager
     * @param tableNameFull Table objectName in format "schemaName.table"
     * @return true if table exists, false otherwise
     */
    public static boolean tableExists(EntityManager entityManager, String tableNameFull) {
        DbObjectMetadata dbObj = parseObject(tableNameFull);
        
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = :schema AND table_name = :table")
            .setParameter("schema", dbObj.schemaName())
            .setParameter("table", dbObj.objectName())
            .getSingleResult();
        
        return count > 0;
    }

    private static void validateIdentifier(String identifier, String type) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException(type + " objectName cannot be null or empty");
        }
        
        // Allow only alphanumeric characters, underscores, and hyphens
        if (!identifier.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException(type + " objectName contains invalid characters: " + identifier);
        }
    }
    
    private static String quoteIdentifier(String identifier) {
        return String.format("\"%s\"", identifier);
    }
}
