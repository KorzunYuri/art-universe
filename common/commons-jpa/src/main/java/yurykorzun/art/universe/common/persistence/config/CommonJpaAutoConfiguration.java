package yurykorzun.art.universe.common.persistence.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import jakarta.persistence.EntityManager;

@AutoConfiguration
@ConditionalOnClass(EntityManager.class)
@ComponentScan(basePackages = "yurykorzun.art.universe.common")
public class CommonJpaAutoConfiguration {
}
