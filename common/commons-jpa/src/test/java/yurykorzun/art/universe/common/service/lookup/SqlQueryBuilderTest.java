package yurykorzun.art.universe.common.service.lookup;

import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqlQueryBuilderTest {

    @Mock
    private Query query;

    @Test
    void append_shouldAppendSqlFragments() {
        // Given
        SqlQueryBuilder builder = new SqlQueryBuilder();
        
        // When
        builder.append("SELECT * FROM ")
               .append("users")
               .append(" WHERE id = ?1");
        
        // Then
        SqlQueryBuilder.QueryData queryData = builder.build();
        assertEquals("SELECT * FROM users WHERE id = ?1", queryData.getSql());
    }

    @Test
    void param_shouldRegisterParameterSetter() {
        // Given
        SqlQueryBuilder builder = new SqlQueryBuilder();
        
        // When
        builder.append("SELECT * FROM users WHERE id = ")
               .param(1, 123)
               .append(" AND name = ")
               .param(2, "John");
        
        SqlQueryBuilder.QueryData queryData = builder.build();
        
        // Then
        queryData.getParametersSetter().accept(query);
        
        verify(query).setParameter(1, 123);
        verify(query).setParameter(2, "John");
    }

    @Test
    void build_shouldValidateParameterIndices() {
        // Given
        SqlQueryBuilder builder = new SqlQueryBuilder();
        
        // When
        builder.append("SELECT * FROM users WHERE id = ?")
               .param(1, 123)
               .append(" AND name = ?")
               .param(2, "John");
        
        // Then
        SqlQueryBuilder.QueryData queryData = builder.build();
        assertNotNull(queryData);
        assertEquals("SELECT * FROM users WHERE id = ? AND name = ?", queryData.getSql());
    }

    @Test
    void build_shouldThrowException_whenParameterIndicesAreNotSequential() {
        // Given
        SqlQueryBuilder builder = new SqlQueryBuilder();
        
        // When
        builder.append("SELECT * FROM users WHERE id = ")
               .param(1, 123)
               .append(" AND name = ")
               .param(3, "John"); // Gap in parameter indices
        
        // Then
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void param_shouldThrowException_whenParameterIndexIsReused() {
        // Given
        SqlQueryBuilder builder = new SqlQueryBuilder();
        
        // When
        builder.append("SELECT * FROM users WHERE id = ")
               .param(1, 123);
        
        // Then
        assertThrows(IllegalArgumentException.class, () -> builder.param(1, 456));
    }

    @Test
    void build_shouldHandleNullParameters() {
        // Given
        SqlQueryBuilder builder = new SqlQueryBuilder();
        
        // When
        builder.append("SELECT * FROM users WHERE id = ")
               .param(1, null)
               .append(" AND name = ")
               .param(2, "John");
        
        SqlQueryBuilder.QueryData queryData = builder.build();
        
        // Then
        queryData.getParametersSetter().accept(query);
        
        verify(query).setParameter(1, null);
        verify(query).setParameter(2, "John");
    }
}
