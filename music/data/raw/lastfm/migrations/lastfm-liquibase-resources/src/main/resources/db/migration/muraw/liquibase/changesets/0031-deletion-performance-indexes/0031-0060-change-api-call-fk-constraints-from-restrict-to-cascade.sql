-- Fix FK constraints on api_call_id from RESTRICT to CASCADE to allow api_call deletion
-- This enables cleanup of orphaned api_call records that are not referenced by entities

-- 1. api_response.api_call_id: Change RESTRICT to CASCADE
-- When api_call is deleted, delete corresponding api_response
ALTER TABLE api_response 
DROP CONSTRAINT api_response_api_call_id_fkey;

ALTER TABLE api_response 
ADD CONSTRAINT api_response_api_call_id_fkey 
FOREIGN KEY (api_call_id) REFERENCES api_call(id) 
ON DELETE CASCADE ON UPDATE NO ACTION;

-- 2. Main entity tables: Change RESTRICT to SET NULL
-- When api_call is deleted, set api_call_id to NULL (preserve entities)

-- artist.api_call_id
ALTER TABLE artist 
DROP CONSTRAINT artist$api_call_id_fk;

ALTER TABLE artist 
ADD CONSTRAINT artist$api_call_id_fk 
FOREIGN KEY (api_call_id) REFERENCES api_call(id) 
ON DELETE SET NULL ON UPDATE NO ACTION;

-- album.api_call_id (missing FK - create new one)
ALTER TABLE album
    DROP CONSTRAINT IF EXISTS album$api_call_id_fk;

ALTER TABLE album 
ADD CONSTRAINT album$api_call_id_fk 
FOREIGN KEY (api_call_id) REFERENCES api_call(id) 
ON DELETE SET NULL ON UPDATE NO ACTION;

-- track.api_call_id  
ALTER TABLE track 
DROP CONSTRAINT track$api_call_id_fk;

ALTER TABLE track 
ADD CONSTRAINT track$api_call_id_fk 
FOREIGN KEY (api_call_id) REFERENCES api_call(id) 
ON DELETE SET NULL ON UPDATE NO ACTION;

-- tag.api_call_id
ALTER TABLE tag 
DROP CONSTRAINT tag$api_call_id_fk;

ALTER TABLE tag 
ADD CONSTRAINT tag$api_call_id_fk 
FOREIGN KEY (api_call_id) REFERENCES api_call(id) 
ON DELETE SET NULL ON UPDATE NO ACTION;

-- 3. attribute_history.api_call_id: Change RESTRICT to CASCADE
-- When api_call is deleted, delete corresponding attribute history
ALTER TABLE attribute_history 
DROP CONSTRAINT attribute_history$api_call_id_fk;

ALTER TABLE attribute_history 
ADD CONSTRAINT attribute_history$api_call_id_fk 
FOREIGN KEY (api_call_id) REFERENCES api_call(id) 
ON DELETE CASCADE ON UPDATE NO ACTION;

-- Add comments to explain the changes
COMMENT ON CONSTRAINT api_response_api_call_id_fkey ON api_response IS 'CASCADE: Delete api_response when api_call is deleted';
COMMENT ON CONSTRAINT artist$api_call_id_fk ON artist IS 'SET NULL: Preserve artist when api_call is deleted';
COMMENT ON CONSTRAINT album$api_call_id_fk ON album IS 'SET NULL: Preserve album when api_call is deleted';
COMMENT ON CONSTRAINT track$api_call_id_fk ON track IS 'SET NULL: Preserve track when api_call is deleted';
COMMENT ON CONSTRAINT tag$api_call_id_fk ON tag IS 'SET NULL: Preserve tag when api_call is deleted';
COMMENT ON CONSTRAINT attribute_history$api_call_id_fk ON attribute_history IS 'CASCADE: Delete attribute history when api_call is deleted';
