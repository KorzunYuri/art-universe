package yurykorzun.art.universe.music.quiz.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.web.config.CommonWebConfig;

@Configuration
@Import(
    CommonWebConfig.class
)
public class WebConfig {
}
