CREATE OR REPLACE VIEW v_mtnc_dead_tup AS
SELECT
    relname,
    n_live_tup,
    n_dead_tup,
    round(case when (n_live_tup + n_dead_tup) > 0 then n_dead_tup::decimal / (n_live_tup + n_dead_tup) else 0 end * 100, 2) as dead_tup_ratio
FROM pg_stat_user_tables
ORDER BY n_dead_tup DESC;