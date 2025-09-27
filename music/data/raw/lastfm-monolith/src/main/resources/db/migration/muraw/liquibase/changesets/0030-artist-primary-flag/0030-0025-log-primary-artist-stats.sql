-- Log the number of artists marked as primary
DO $$
DECLARE
    primary_count INTEGER;
    total_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO primary_count FROM artist WHERE is_primary = TRUE;
    SELECT COUNT(*) INTO total_count FROM artist;

    RAISE NOTICE 'Marked % out of % artists as primary (%.1f%%)',
        primary_count,
        total_count,
        CASE WHEN total_count = 0 THEN 0.0 ELSE (primary_count::DECIMAL / total_count * 100) END;
END $$;
