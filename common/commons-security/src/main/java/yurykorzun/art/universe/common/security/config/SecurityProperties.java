package yurykorzun.art.universe.common.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.security")
public class SecurityProperties {

    private List<String> publicPaths = List.of("/health", "/actuator/**");
    private String allowedOrigins = "*";
}
