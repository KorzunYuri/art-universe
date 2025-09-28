-- Log migration results for track deduplication

-- Create table for migration results logging
CREATE TEMP TABLE migration_log AS
SELECT 
    'url_conflicts_resolved' as operation,
    COUNT(*) as count
FROM track_url_conflicts

UNION ALL

SELECT 
    'duplicate_tracks_removed' as operation,
    COUNT(*) as count
FROM track_duplicates

UNION ALL

SELECT 
    'track_urls_normalized' as operation,
    COUNT(*) as count
FROM track 
WHERE url LIKE '%/_/%';

-- Output results (will be visible in Liquibase logs)
DO $$
DECLARE
    rec RECORD;
BEGIN
    FOR rec IN SELECT * FROM migration_log LOOP
        RAISE NOTICE 'Migration result: % = %', rec.operation, rec.count;
    END LOOP;
END $$;
