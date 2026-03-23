package yurykorzun.art.universe.common.auth.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import yurykorzun.art.universe.common.security.model.Role;

import java.sql.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Converts between Set&lt;Role&gt; and PostgreSQL text[] column.
 * Uses JDBC Array API directly via Hibernate's unwrap mechanism.
 */
@Converter
public class RoleArrayConverter implements AttributeConverter<Set<Role>, String> {

    @Override
    public String convertToDatabaseColumn(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return "{VIEWER}";
        }
        return "{" + roles.stream()
            .map(Enum::name)
            .collect(Collectors.joining(",")) + "}";
    }

    @Override
    public Set<Role> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return EnumSet.of(Role.VIEWER);
        }
        // PostgreSQL text[] literal format: {VIEWER,ADMIN}
        String cleaned = dbData.replaceAll("[{}]", "");
        if (cleaned.isEmpty()) {
            return EnumSet.of(Role.VIEWER);
        }
        return Arrays.stream(cleaned.split(","))
            .map(String::trim)
            .map(Role::valueOf)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(Role.class)));
    }
}
