package yurykorzun.art.universe.music.quiz.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
    "yurykorzun.art.universe.common.persistence"
})
public class CommonConfig {
}
