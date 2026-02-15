package yurykorzun.art.universe.common.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.common.config.CommonConfig;

public class CommonTestConfig {

    /**
     * Return default object mapper used in the project
     */
    public static ObjectMapper getObjectMapper() {
        return CommonConfig.getObjectMapper();
    }

}
