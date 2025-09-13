package yurykorzun.art.universe.music.quiz.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for convenient adding processors to the context (mainly for tests).
 */
@Configuration
@ComponentScan(basePackages = "yurykorzun.art.universe.music.quiz.service.step")
public class StepProcessorConfig {
}
