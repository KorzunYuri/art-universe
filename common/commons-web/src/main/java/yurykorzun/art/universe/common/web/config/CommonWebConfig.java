package yurykorzun.art.universe.common.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.web.exception.CommonGlobalExceptionHandler;

@Configuration
@Import({
    CommonGlobalExceptionHandler.class
})
public class CommonWebConfig {
}
