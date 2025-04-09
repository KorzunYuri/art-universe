package yurykorzun.art.universe.music.data.raw.lastfm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
public class DynamicPropertySourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        log.info("Refreshing properties");
        TestPropertyValues.of(
                "spring.datasource.url=" + PostgresTestContainerHolder.getContainer().getJdbcUrl(),
                "spring.datasource.username=" + PostgresTestContainerHolder.getContainer().getUsername(),
                "spring.datasource.password=" + PostgresTestContainerHolder.getContainer().getPassword()
        ).applyTo(applicationContext.getEnvironment());
    }
}
