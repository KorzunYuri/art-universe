CREATE OR REPLACE VIEW v_attr_value_actual AS
SELECT  *
FROM    v_attr_value
WHERE   valid_till = '9999-12-31';