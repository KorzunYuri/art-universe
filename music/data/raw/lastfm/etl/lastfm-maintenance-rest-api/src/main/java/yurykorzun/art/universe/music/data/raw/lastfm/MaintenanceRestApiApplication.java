package yurykorzun.art.universe.music.data.raw.lastfm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {
    "yurykorzun.art.universe.music.data.raw.lastfm.maintenance"
})
public class MaintenanceRestApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(MaintenanceRestApiApplication.class, args);
    }
}
