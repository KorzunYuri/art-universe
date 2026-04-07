-- Reset the most recent COMPLETED getInfo responses to PENDING for reprocessing.
-- This allows the existing response parser to re-process them and extract bio/wiki text
-- into the new entity_text_content table.
--
-- For each entity, only the most recent response (by api_response.created_at) is selected.
-- api_call types: 4=artist.getInfo, 10=track.getInfo, 11=album.getInfo
-- api_response statuses: 2=PENDING, 5=COMPLETED

UPDATE mu_raw_lastfm.api_response
SET status = 2,
    updated_at = CURRENT_TIMESTAMP
WHERE id IN (
    SELECT DISTINCT ON (ac.entity_type, ac.entity_id)
        r.id
    FROM mu_raw_lastfm.api_response r
    JOIN mu_raw_lastfm.api_call ac ON r.api_call_id = ac.id
    WHERE ac.type IN (4, 10, 11)
      AND r.status = 5
    ORDER BY ac.entity_type, ac.entity_id, r.created_at DESC
);
