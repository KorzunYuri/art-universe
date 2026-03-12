-- staging-applicator (mu_raw_spotify_appl)
-- Reads: staging_iteration (find SEALED iterations)
--        search_attempt (find MATCHED attempts)
--        artist, album, track (synthetic ID resolution joins)
--        stg_*_N (read staged rows — covered by ALTER DEFAULT PRIVILEGES in 0000-schema-grants)
-- Writes: artist, album, track, entity_relation (upsert raw entities)
--         search_attempt (UPDATE status to BOUND)
--         staging_iteration (UPDATE status to APPLYING/COMPLETED/FAILED)
--         stg_*_N (UPDATE for synthetic ID resolution — same default privs)
-- DDL: DROP TABLE stg_*_N (cleanup — covered by staging schema ownership in init.sh 0011)

GRANT INSERT, UPDATE ON TABLE artist           TO mu_raw_spotify_appl;
GRANT INSERT, UPDATE ON TABLE album            TO mu_raw_spotify_appl;
GRANT INSERT, UPDATE ON TABLE track            TO mu_raw_spotify_appl;
GRANT INSERT, UPDATE ON TABLE entity_relation  TO mu_raw_spotify_appl;
GRANT UPDATE         ON TABLE search_attempt   TO mu_raw_spotify_appl;
GRANT UPDATE         ON TABLE staging_iteration TO mu_raw_spotify_appl;
