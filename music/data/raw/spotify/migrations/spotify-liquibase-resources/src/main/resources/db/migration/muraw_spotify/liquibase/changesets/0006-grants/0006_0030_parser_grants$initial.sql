-- response-parser (mu_raw_spotify_parser)
-- Reads: api_response (fetch PENDING responses)
--        api_call (look up call type)
--        search_attempt (find attempt by api_call_id to get search string)
--        staging_iteration (find/create OPEN iteration)
--        stg_*_template (read structure via LIKE ... INCLUDING ALL in CREATE TABLE)
-- Writes: api_response (UPDATE status to COMPLETED/PROCESSING_ERROR)
--         search_attempt (UPDATE best_match_score, matched_spotify_id, status)
--         staging_iteration (INSERT new, UPDATE records_staged / seal)
--         stg_*_N (CREATE TABLE + INSERT — covered by schema CREATE privilege in init.sh)

GRANT UPDATE         ON TABLE api_response      TO mu_raw_spotify_parser;
GRANT UPDATE         ON TABLE search_attempt    TO mu_raw_spotify_parser;
GRANT INSERT, UPDATE ON TABLE staging_iteration TO mu_raw_spotify_parser;
