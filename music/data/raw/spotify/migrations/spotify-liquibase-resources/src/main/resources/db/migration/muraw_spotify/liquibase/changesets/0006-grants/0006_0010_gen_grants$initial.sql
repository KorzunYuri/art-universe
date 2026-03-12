-- calls-generator (mu_raw_spotify_gen)
-- Reads: api_call (check for existing due calls)
--        search_attempt (check for pending/searched attempts)
--        v_artist, v_album, v_track (find entities without active calls — via mu_view)
-- Writes: api_call (INSERT new calls)
--         search_attempt (INSERT new attempts, UPDATE to set api_call_id)

GRANT INSERT         ON TABLE api_call       TO mu_raw_spotify_gen;
GRANT INSERT, UPDATE ON TABLE search_attempt TO mu_raw_spotify_gen;
