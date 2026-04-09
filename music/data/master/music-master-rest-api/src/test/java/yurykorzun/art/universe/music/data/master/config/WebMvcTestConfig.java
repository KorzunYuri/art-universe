package yurykorzun.art.universe.music.data.master.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for WebMvc tests to handle enum conversion
 */
@Configuration
@Import(WebMvcConfig.class)
public class WebMvcTestConfig implements WebMvcConfigurer {
}
