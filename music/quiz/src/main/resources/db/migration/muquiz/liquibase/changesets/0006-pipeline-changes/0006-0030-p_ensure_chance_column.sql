-- Ensures input table has chance column, creates it if missing
CREATE OR REPLACE FUNCTION p_ensure_chance_column(
    p_schema_name TEXT,
    p_table_name TEXT
) RETURNS TEXT AS $$
DECLARE
    has_chance BOOLEAN;
    is_view BOOLEAN;
    new_table_name TEXT;
    full_new_table TEXT;
BEGIN
    -- Check if chance column exists
    SELECT EXISTS (
        SELECT
            1
        FROM
            information_schema.columns i
        WHERE   1=1
            AND i.table_schema  = p_schema_name
            AND i.table_name    = p_table_name
            AND i.column_name   = 'chance'
    ) INTO has_chance;
    
    -- Return original if chance column exists
    IF has_chance THEN
        RETURN p_schema_name || '.' || p_table_name;
    END IF;
    
    -- Check if it's a view
    SELECT EXISTS (
        SELECT
            1
        FROM
            information_schema.views i
        WHERE   1=1
            AND i.table_schema  = p_schema_name
            AND i.table_name    = p_table_name
    ) INTO is_view;
    
    IF is_view THEN
        -- Create new table from view with chance column
        new_table_name := p_table_name || '_chance';
        full_new_table := 'mu_quiz_stg.' || new_table_name;
        
        EXECUTE format('CREATE TABLE mu_quiz_stg.%I AS SELECT *, 1.0 as chance FROM %I.%I',
            new_table_name, p_schema_name, p_table_name);
            
        RETURN full_new_table;
    ELSE
        -- Add chance column to existing table
        EXECUTE format('ALTER TABLE %I.%I ADD COLUMN chance DECIMAL DEFAULT 1.0',
            p_schema_name, p_table_name);
        EXECUTE format('UPDATE %I.%I SET chance = 1.0 WHERE chance IS NULL',
            p_schema_name, p_table_name);

        RETURN p_schema_name || '.' || p_table_name;
    END IF;
END;
$$ LANGUAGE plpgsql;
