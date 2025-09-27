package yurykorzun.art.universe.music.data.raw.lastfm.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.common.config.CommonWebConfig;

public class TestBeansConfig {

    private static final CommonWebConfig commonWebConfig = new CommonWebConfig();

    /**
     * Return default object mapper used in the project
     */
    public static ObjectMapper getObjectMapper() {
        return commonWebConfig.objectMapper();
    }

}
