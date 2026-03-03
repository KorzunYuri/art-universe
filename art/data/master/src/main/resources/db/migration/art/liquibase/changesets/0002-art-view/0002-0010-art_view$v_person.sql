-- Create read-only view of person in art_view schema for cross-schema access
CREATE OR REPLACE VIEW art_view.v_person AS
SELECT id, name FROM art.person;
