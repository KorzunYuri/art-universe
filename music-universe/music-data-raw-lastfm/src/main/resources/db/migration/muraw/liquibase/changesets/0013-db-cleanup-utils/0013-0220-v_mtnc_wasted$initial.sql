CREATE OR REPLACE VIEW v_mtnc_wasted AS
SELECT
    schemaname
     ,	relname
     ,	pg_relation_size(relid) 		as rel_size
     ,	pg_total_relation_size(relid) 	as rel_size_total
     ,	round(100 * (pg_total_relation_size(relid) - pg_relation_size(relid)) / pg_total_relation_size(relid)) as wasted_percent
FROM
    pg_catalog.pg_statio_user_tables
where
    pg_total_relation_size(relid) > 0
ORDER BY wasted_percent DESC;