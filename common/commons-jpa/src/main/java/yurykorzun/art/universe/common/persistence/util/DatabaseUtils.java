package yurykorzun.art.universe.common.persistence.util;

import jakarta.persistence.EntityManager;

/**
 * Database utility methods for safe DDL operations
 */
public class DatabaseUtils {
    
    private DatabaseUtils() {
        // Utility class
    }
    
    /**
     * Safely drops a table with SQL injection protection
     * @param entityManager JPA EntityManager
     * @param fullTableName Table name in format "schema.table"
     */
    public static void dropTable(EntityManager entityManager, String fullTableName) {
        validateTableName(fullTableName);
        
        String[] parts = fullTableName.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Table name must be in format 'schema.table', got: " + fullTableName);
        }
        
        String schema = parts[0];
        String table = parts[1];
        
        // Validate schema and table names contain only safe characters
        validateIdentifier(schema, "schema");
        validateIdentifier(table, "table");
        
        // Use format with %I for safe identifier substitution
        String sql = String.format("DROP TABLE IF EXISTS %s.%s", 
            quoteIdentifier(schema), quoteIdentifier(table));
        
        entityManager.createNativeQuery(sql).executeUpdate();
    }
    
    /**
     * Checks if a table exists in the database
     * @param entityManager JPA EntityManager
     * @param fullTableName Table name in format "schema.table"
     * @return true if table exists, false otherwise
     */
    public static boolean tableExists(EntityManager entityManager, String fullTableName) {
        validateTableName(fullTableName);
        
        String[] parts = fullTableName.split("\\.");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Table name must be in format 'schema.table', got: " + fullTableName);
        }
        
        String schema = parts[0];
        String table = parts[1];
        
        validateIdentifier(schema, "schema");
        validateIdentifier(table, "table");
        
        Long count = (Long) entityManager.createNativeQuery(
            "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = :schema AND table_name = :table")
            .setParameter("schema", schema)
            .setParameter("table", table)
            .getSingleResult();
        
        return count > 0;
    }
    
    private static void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Table name cannot be null or empty");
        }
    }
    
    private static void validateIdentifier(String identifier, String type) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException(type + " name cannot be null or empty");
        }
        
        // Allow only alphanumeric characters, underscores, and hyphens
        if (!identifier.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException(type + " name contains invalid characters: " + identifier);
        }
    }
    
    private static String quoteIdentifier(String identifier) {
        return "\"" + identifier + "\"";
    }
}
