package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Builder for SQL queries with parameter setters
 */
public class SqlQueryBuilder {
    
    private final StringBuilder sqlBuilder = new StringBuilder();
    private final Map<Integer, Consumer<Query>> parameterSetters = new HashMap<>();

    /**
     * Appends SQL fragment to the query
     */
    public SqlQueryBuilder append(String sql) {
        sqlBuilder.append(sql);
        return this;
    }

    /**
     * Adds a parameter with index and value
     * For NULL values, consider using paramWithCast to specify the SQL type
     */
    public SqlQueryBuilder param(int index, Object value) {
        if (parameterSetters.containsKey(index)) {
            throw new IllegalArgumentException(String.format("Parameter value can be assigned only once! Index: %s", index));
        }

        parameterSetters.put(index, query -> query.setParameter(index, value));
        return this;
    }

    /**
     * Builds the final QueryData object containing SQL and parameter setters
     */
    public QueryData build() {
        for (int i = 1; i <= parameterSetters.size(); i++) {
            if (!parameterSetters.containsKey(i)) {
                throw new IllegalStateException(String.format(
                    "Query parameters must have strict numeration, starting from 1, got: %s",
                    parameterSetters.keySet()));
            }
        }
        return new QueryData(
            sqlBuilder.toString(),
            (query) -> {
                for (Consumer<Query> setter : parameterSetters.values()) {
                    setter.accept(query);
                }
            }
        );
    }
    
    /**
     * Data class containing SQL and parameter setters
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QueryData {
        private String sql;
        private java.util.function.Consumer<Query> parametersSetter;
    }
}
