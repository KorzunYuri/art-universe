package yurykorzun.art.universe.common.pgnotify;

import org.postgresql.PGConnection;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ConditionalOnClass(PGConnection.class)
@ComponentScan(basePackages = "yurykorzun.art.universe.common.pgnotify")
public class PgNotifyAutoConfiguration {
}
