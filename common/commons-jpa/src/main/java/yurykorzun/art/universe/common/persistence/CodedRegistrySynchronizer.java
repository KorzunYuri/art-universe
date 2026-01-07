package yurykorzun.art.universe.common.persistence;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Map;

/**
 * Synchronizes {@link Coded} instances with database, for convenience (e.g. for replacing codes with expressive names in queries)
 */
@Component
public class CodedRegistrySynchronizer {

    private final JdbcTemplate jdbcTemplate;

    public CodedRegistrySynchronizer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    @Transactional
    public void syncDictionary() {
        // persist each domain
        for (Class<? extends Coded> domainClass : CodedRegistry.getDomains()) {
            String domainName = domainClass.getSimpleName();

            Map<Integer, ? extends Coded> registry = CodedRegistry.getRegistry(domainClass);
            // persist all the Coded instances registered for the domain
            for (Coded coded : registry.values()) {
                jdbcTemplate.update("""
                        INSERT INTO dictionary (domain, code, name)
                        VALUES (?, ?, ?)
                        ON CONFLICT (domain, code) DO UPDATE SET name = EXCLUDED.name
                    """,
                    domainName, coded.getCode(), coded.getName());
            }
        }
    }
}
