package yurykorzun.art.universe.common.security.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "auth.jwt")
public class JwtProperties {

    private String secret;
    private int expirationMinutes = 15;
    private String issuer = "art-universe";
}
