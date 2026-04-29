package yurykorzun.art.universe.music.data.semantic.applicator.applier.support;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.semantic.applicator.applier.ApplicationContext;

/**
 * Resolves an entity id from a proposal payload. Callers pass the id-field name
 * ({@code entity_id}, {@code source_entity_id}, {@code master_entity_id}, ...)
 * and the matching synth-ref field. The resolver returns the concrete id,
 * transparently looking up synth ids in the {@link ApplicationContext}.
 * <p>
 * The role ("source", "target", "master", ...) is used only for diagnostic
 * error messages.
 */
@Component
public class EntityReferenceResolver {

    /** Requires that either idField or refField yields a resolvable id. */
    public Long require(
        JsonNode payload,
        String idField,
        String refField,
        ApplicationContext context,
        String proposalType,
        String role
    ) {
        Long resolved = resolveOrNull(payload, idField, refField, context, proposalType, role);
        if (resolved != null) {
            return resolved;
        }
        throw new IllegalArgumentException(
            proposalType + " requires '" + idField + "' or resolvable '" + refField + "'"
                + (role == null ? "" : " (" + role + ")")
        );
    }

    /** Returns null if neither field is present; throws if a ref is given but unresolved. */
    public Long resolveOrNull(
        JsonNode payload,
        String idField,
        String refField,
        ApplicationContext context,
        String proposalType,
        String role
    ) {
        if (payload.hasNonNull(idField)) {
            return payload.get(idField).asLong();
        }
        String ref = payload.path(refField).asText(null);
        if (ref == null || ref.isBlank()) {
            return null;
        }
        Long resolved = context.resolveSynthId(ref);
        if (resolved == null) {
            throw new IllegalStateException(
                proposalType + ": unresolved " + refField + "='" + ref + "'"
                    + (role == null ? "" : " (" + role + ")")
            );
        }
        return resolved;
    }
}
