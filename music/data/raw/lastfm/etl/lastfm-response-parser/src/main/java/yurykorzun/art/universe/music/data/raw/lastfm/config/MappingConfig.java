package yurykorzun.art.universe.music.data.raw.lastfm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingConfig {

    public static final String LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME = "lastfmApiResponseObjectMapper";

    @Bean(name = LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME)
    public ObjectMapper apiResponseObjectMapper() {
        return new ObjectMapper();
    }

}
