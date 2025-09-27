-- Table sizes by category with schema total
WITH table_categories AS (
    SELECT 
        schemaname,
        tablename,
        CASE 
            WHEN tablename IN ('artist', 'track', 'tag', 'album') THEN 'core_entities'
            WHEN tablename IN ( 'artist_album', 'artist_track', 'artist_tag', 'artist_artist',
                                'album_track', 'album_tag',
                                'track_tag') THEN 'relationships'
            WHEN tablename IN ('attribute_history', 'attribute_snapshot', 'data_snapshot') THEN 'attributes'
            WHEN tablename IN ('mtnc_deleted_entities', 'blacklist_entity_url') THEN 'maintenance'
            WHEN tablename IN ('api_call', 'api_response') THEN 'api_processing'
            ELSE 'other'
        END as table_category,
        pg_total_relation_size(schemaname||'.'||tablename) as table_size_bytes
    FROM pg_tables 
    WHERE schemaname = 'mu_raw_lastfm'
),
category_totals AS (
    SELECT 
        table_category,
        SUM(table_size_bytes) as category_size_bytes
    FROM table_categories
    GROUP BY table_category
),
schema_total AS (
    SELECT 
        'schema_total' as table_category,
        SUM(table_size_bytes) as category_size_bytes
    FROM table_categories
),
individual_tables AS (
    SELECT 
        tablename as table_name,
        table_category,
        table_size_bytes
    FROM table_categories
)
-- Return both category totals and individual table sizes
SELECT 
    table_category,
    NULL as table_name,
    category_size_bytes as size_bytes,
    'category' as metric_type
FROM category_totals
UNION ALL
SELECT 
    table_category,
    NULL as table_name,
    category_size_bytes as size_bytes,
    'schema' as metric_type
FROM schema_total
UNION ALL
SELECT 
    table_category,
    table_name,
    table_size_bytes as size_bytes,
    'table' as metric_type
FROM individual_tables
ORDER BY metric_type, table_category, table_name;
