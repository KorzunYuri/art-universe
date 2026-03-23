package yurykorzun.art.universe.common.security.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import yurykorzun.art.universe.common.security.jwt.JwtAuthenticationFilter;
import yurykorzun.art.universe.common.security.jwt.JwtProperties;
import yurykorzun.art.universe.common.security.jwt.JwtTokenProvider;

import java.util.Arrays;
import java.util.List;

@AutoConfiguration
@Conditional(JwtSecretCondition.class)
@EnableWebSecurity
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class})
@EnableMethodSecurity
@Import(SecurityExceptionHandler.class)
@RequiredArgsConstructor
public class CommonSecurityConfig {

    private final SecurityProperties securityProperties;

    @Bean
    public RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.fromHierarchy("""
                ROLE_ADMIN > ROLE_MASTER_CURATOR
                ROLE_ADMIN > ROLE_LASTFM_CURATOR
                ROLE_ADMIN > ROLE_SPOTIFY_CURATOR
                ROLE_ADMIN > ROLE_QUIZ_MASTER
                ROLE_ADMIN > ROLE_CONFIG_MANAGER
                ROLE_ADMIN > ROLE_MAINTAINER
                ROLE_MAINTAINER > ROLE_SURVEYOR
                ROLE_MASTER_CURATOR > ROLE_VIEWER
                ROLE_LASTFM_CURATOR > ROLE_VIEWER
                ROLE_SPOTIFY_CURATOR > ROLE_VIEWER
                ROLE_QUIZ_MASTER > ROLE_VIEWER
                ROLE_CONFIG_MANAGER > ROLE_VIEWER
                ROLE_SURVEYOR > ROLE_VIEWER
                """);
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(RoleHierarchy roleHierarchy) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setRoleHierarchy(roleHierarchy);
        return handler;
    }

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        return new JwtAuthenticationFilter(tokenProvider);
    }

    /**
     * Prevent Spring Boot from auto-registering JwtAuthenticationFilter as a servlet filter.
     * Without this, the filter runs twice: once as a servlet filter (outside the security chain)
     * and once inside the security chain. OncePerRequestFilter skips the second execution,
     * but by then SecurityContextHolderFilter has already cleared the context set during
     * the first (out-of-chain) execution — resulting in 403 for authenticated requests.
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilterRegistration(
            JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtAuthenticationFilter jwtFilter) throws Exception {

        String[] publicPaths = securityProperties.getPublicPaths().toArray(String[]::new);

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(publicPaths).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(securityProperties.getAllowedOrigins().split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
