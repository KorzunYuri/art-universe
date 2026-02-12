-- Update v_relation_type_applicability to include is_default
CREATE OR REPLACE VIEW mu_view.v_relation_type_applicability AS
SELECT id, relation_type_id, source_entity_type, target_entity_type, is_default
FROM mu.relation_type_applicability;

-- Update v_artist_album to include primary_artist_id references from album table
-- Junction table rows have is_primary = false, primary_artist_id rows have is_primary = true
-- Synthetic IDs for primary_artist_id rows use negative album.id to avoid collisions
CREATE OR REPLACE VIEW mu_view.v_artist_album AS
SELECT aa.id, aa.artist_id, aa.album_id, aa.relation_type_id, false AS is_primary
FROM mu.artist_album aa
UNION ALL
SELECT -a.id AS id, a.primary_artist_id AS artist_id, a.id AS album_id,
       rta.relation_type_id, true AS is_primary
FROM mu.album a
LEFT JOIN mu.relation_type_applicability rta
    ON rta.source_entity_type = 1 AND rta.target_entity_type = 2
    AND rta.is_default = TRUE;

-- Update v_artist_track to include primary_artist_id references from track table
CREATE OR REPLACE VIEW mu_view.v_artist_track AS
SELECT at2.id, at2.artist_id, at2.track_id, at2.relation_type_id, false AS is_primary
FROM mu.artist_track at2
UNION ALL
SELECT -t.id AS id, t.primary_artist_id AS artist_id, t.id AS track_id,
       rta.relation_type_id, true AS is_primary
FROM mu.track t
LEFT JOIN mu.relation_type_applicability rta
    ON rta.source_entity_type = 1 AND rta.target_entity_type = 3
    AND rta.is_default = TRUE;
