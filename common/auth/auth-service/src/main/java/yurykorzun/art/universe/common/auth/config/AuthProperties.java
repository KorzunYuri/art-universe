package yurykorzun.art.universe.common.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
@Getter
@Setter
public class AuthProperties {

    private Google google = new Google();
    private RefreshToken refreshToken = new RefreshToken();

    @Getter
    @Setter
    public static class Google {
        private String clientId;
    }

    @Getter
    @Setter
    public static class RefreshToken {
        private int expirationDays = 7;
    }
}
