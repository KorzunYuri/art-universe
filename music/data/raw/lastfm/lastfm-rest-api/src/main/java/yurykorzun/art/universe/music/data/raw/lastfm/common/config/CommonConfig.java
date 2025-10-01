package yurykorzun.art.universe.music.data.raw.lastfm.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.CodedAutoregistrator;

@Configuration
//@ComponentScan(basePackages = {
//    "yurykorzun.art.universe.common.persistence"
//})
@Import(value = {
    CodedAutoregistrator.class
})
public class CommonConfig {
}
