package yurykorzun.art.universe.music.data.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
    "yurykorzun.art.universe.music.data.master.entity",
    "yurykorzun.art.universe.common.persistence.converter",
    "yurykorzun.art.universe.common.domain.entity"
})
public class MusicUniverseDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicUniverseDataApplication.class, args);
    }
}
