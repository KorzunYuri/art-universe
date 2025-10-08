package yurykorzun.art.universe.music.data.raw.lastfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class LastfmEtlRestApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LastfmEtlRestApiApplication.class, args);
    }
}
