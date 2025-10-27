-- Update p_ensure_chance_column to use input_table parameter
DROP FUNCTION IF EXISTS p_ensure_chance_column(TEXT, TEXT);

CREATE OR REPLACE FUNCTION p_ensure_chance_column(
    input_table TEXT
) RETURNS TEXT AS $$
DECLARE
    input_parts TEXT[];
    has_chance BOOLEAN;
    is_view BOOLEAN;
    new_table_name TEXT;
    full_new_table TEXT;
BEGIN
    input_parts := p_parse_table_name(input_table);
    
    -- Check if chance column exists
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.columns i
        WHERE i.table_schema = input_parts[1]
            AND i.table_name = input_parts[2]
            AND i.column_name = 'chance'
    ) INTO has_chance;
    
    -- Return original if chance column exists
    IF has_chance THEN
        RETURN input_table;
    END IF;
    
    -- Check if it's a view
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.views i
        WHERE i.table_schema = input_parts[1]
            AND i.table_name = input_parts[2]
    ) INTO is_view;
    
    IF is_view THEN
        -- Create new table from view with chance column
        new_table_name := input_parts[2] || '_chance';
        full_new_table := 'mu_quiz_stg.' || new_table_name;
        
        EXECUTE format('CREATE TABLE mu_quiz_stg.%I AS SELECT *, 1.0 as chance FROM %I.%I',
            new_table_name, input_parts[1], input_parts[2]);
            
        RETURN full_new_table;
    ELSE
        -- Add chance column to existing table
        EXECUTE format('ALTER TABLE %I.%I ADD COLUMN chance DECIMAL DEFAULT 1.0',
            input_parts[1], input_parts[2]);
        EXECUTE format('UPDATE %I.%I SET chance = 1.0 WHERE chance IS NULL',
            input_parts[1], input_parts[2]);

        RETURN input_table;
    END IF;
END;
$$ LANGUAGE plpgsql;
