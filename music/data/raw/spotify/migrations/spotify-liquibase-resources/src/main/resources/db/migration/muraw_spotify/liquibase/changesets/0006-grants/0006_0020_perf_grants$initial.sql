-- calls-performer (mu_raw_spotify_perf)
-- Reads: api_call (fetch CREATED unexpired calls)
-- Writes: api_call (UPDATE status to SUCCESSFUL/FAILED)
--         api_response (INSERT raw JSON responses)

GRANT UPDATE ON TABLE api_call      TO mu_raw_spotify_perf;
GRANT INSERT ON TABLE api_response  TO mu_raw_spotify_perf;
