package yurykorzun.art.universe.music.data.semantic.applicator.applier.support;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

/**
 * Pure helpers for reading fields off a proposal payload JsonNode. Kept as a
 * utility (not a bean) so strategies and services can share parsing without DI.
 */
public final class ProposalPayloads {

    private ProposalPayloads() {
    }

    public static String requireString(JsonNode payload, String field, String proposalType) {
        String value = payload.path(field).asText(null);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(proposalType + " payload missing required '" + field + "'");
        }
        return value;
    }

    public static String optionalString(JsonNode payload, String field) {
        String value = payload.path(field).asText(null);
        return (value == null || value.isBlank()) ? null : value;
    }

    public static Long optionalLong(JsonNode payload, String field) {
        return payload.hasNonNull(field) ? payload.get(field).asLong() : null;
    }

    public static Long requireLong(JsonNode payload, String field, String proposalType) {
        Long value = optionalLong(payload, field);
        if (value == null) {
            throw new IllegalArgumentException(proposalType + " payload missing required '" + field + "'");
        }
        return value;
    }

    public static LocalDate readDate(JsonNode payload, String field) {
        String raw = optionalString(payload, field);
        if (raw == null) {
            return null;
        }
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }
}
