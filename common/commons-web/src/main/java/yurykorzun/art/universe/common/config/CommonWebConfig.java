package yurykorzun.art.universe.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.exception.CommonGlobalExceptionHandler;

@Configuration
@Import({
    CommonGlobalExceptionHandler.class
})
public class CommonWebConfig {
}
