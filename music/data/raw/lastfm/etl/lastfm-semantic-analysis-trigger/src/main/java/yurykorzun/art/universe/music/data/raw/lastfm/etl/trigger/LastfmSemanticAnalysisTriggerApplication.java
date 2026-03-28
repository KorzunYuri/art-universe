package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@SpringBootApplication
@EntityScan(basePackages = "yurykorzun.art.universe.music.data.raw.lastfm")
@EnableJpaRepositories(basePackages = "yurykorzun.art.universe.music.data.raw.lastfm")
public class LastfmSemanticAnalysisTriggerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LastfmSemanticAnalysisTriggerApplication.class, args);
    }
}
