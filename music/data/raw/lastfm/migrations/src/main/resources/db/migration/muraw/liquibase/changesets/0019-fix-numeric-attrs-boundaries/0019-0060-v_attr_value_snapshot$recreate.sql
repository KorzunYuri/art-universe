CREATE OR REPLACE VIEW v_attr_value_snapshot AS
SELECT  va.*
FROM
    attribute_snapshot asnap
        JOIN
    data_snapshot ds
    ON      ds.id = asnap.data_snapshot_id_cur
        JOIN
    api_call ac
    ON      ac.data_snapshot_id = ds.id
        JOIN
    v_attr_value va
    ON      coalesce(va.scope_entity_type,  -1) = coalesce(asnap.scope_entity_type, -1)
        AND coalesce(va.scope_entity_id,    -1) = coalesce(asnap.scope_entity_id,   -1)
        AND va.entity_type  = asnap.entity_type
        and va.attribute_id = asnap.attribute_id
;