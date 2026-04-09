package yurykorzun.art.universe.music.data.master.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Fallback CORS configuration for when security is not enabled (no AUTH_JWT_SECRET).
 * When commons-security is active, CORS is handled by {@link yurykorzun.art.universe.common.security.config.CommonSecurityConfig}.
 */
@Configuration
@ConditionalOnMissingBean(SecurityFilterChain.class)
@Slf4j
public class WebSecurityConfig {

    @Value("${server.allowed-origins}")
    private String allowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        log.info("Security not enabled (no AUTH_JWT_SECRET). Using fallback CORS config. Allowed origins: {}", allowedOrigins);
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(allowedOrigins.split(","))
                    .allowedMethods("*");
            }
        };
    }

}
