package yurykorzun.art.universe.music.data.semantic.applicator.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DictionaryRepository {

    private final JdbcTemplate jdbcTemplate;

    public DictionaryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void upsertRecord(String domain, short code, String name) {
        jdbcTemplate.update(
            "INSERT INTO mu.dictionary (domain, code, name) VALUES (?, ?, ?) "
                + "ON CONFLICT (domain, code) DO UPDATE SET name = EXCLUDED.name",
            domain, code, name
        );
    }

    public Short findMaxCodeForDomain(String domain) {
        try {
            return jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(code), 0) FROM mu.dictionary WHERE domain = ?",
                Short.class,
                domain
            );
        } catch (EmptyResultDataAccessException e) {
            return 0;
        }
    }
}
