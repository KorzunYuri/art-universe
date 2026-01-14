package yurykorzun.art.universe.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.common.config.CommonConfig;

public class CommonTestConfig {

    private static final CommonConfig commonConfig = new CommonConfig();

    /**
     * Return default object mapper used in the project
     */
    public static ObjectMapper getObjectMapper() {
        return commonConfig.objectMapper();
    }

}
