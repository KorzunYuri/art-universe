-- renew artist_id for tracks based on entity_relation
UPDATE track t
SET artist_id = er.scope_entity_id
FROM entity_relation er
WHERE er.entity_type = 3 -- TRACK
  AND er.entity_id = t.id
  AND er.scope_entity_type = 1 -- ARTIST
  AND t.artist_id IS NULL;
