-- Helper function to parse and validate table name in format schema.table
CREATE OR REPLACE FUNCTION p_parse_table_name(
    table_name TEXT
) RETURNS TEXT[] AS $$
DECLARE
    parts TEXT[];
BEGIN
    parts := string_to_array(table_name, '.');
    IF array_length(parts, 1) != 2 THEN
        RAISE EXCEPTION 'Table name must be in format schema.table, got: %', table_name;
    END IF;
    RETURN parts;
END;
$$ LANGUAGE plpgsql;
