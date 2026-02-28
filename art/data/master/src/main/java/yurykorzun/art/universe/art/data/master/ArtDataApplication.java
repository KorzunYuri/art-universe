package yurykorzun.art.universe.art.data.master;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan(basePackages = {
    "yurykorzun.art.universe.art.data.models.entity",
    "yurykorzun.art.universe.common.persistence.converter",
    "yurykorzun.art.universe.common.domain.entity"
})
public class ArtDataApplication {

    public static void main(String[] args) {
        SpringApplication.run(ArtDataApplication.class, args);
    }
}
