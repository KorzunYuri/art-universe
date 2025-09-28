-- Mark artists created via artist.getInfo as primary artists
-- These represent the canonical version of artists with complete metadata

UPDATE artist 
SET is_primary = TRUE 
WHERE id IN (
    SELECT a.id
    from
        artist a
    join
        api_call ac
        on	a.api_call_id = ac.id
    where a.mbid = ac.parameters::jsonb->>'mbid'
      and ac.type = 4
);
