package yurykorzun.art.universe.common.security.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Activates only when {@code auth.jwt.secret} is present AND non-empty.
 * This prevents security from activating in dev mode or tests where the
 * property is defined as empty (e.g., {@code auth.jwt.secret: ${AUTH_JWT_SECRET:}}).
 */
public class JwtSecretCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String secret = context.getEnvironment().getProperty("auth.jwt.secret");
        return secret != null && !secret.isBlank();
    }
}
