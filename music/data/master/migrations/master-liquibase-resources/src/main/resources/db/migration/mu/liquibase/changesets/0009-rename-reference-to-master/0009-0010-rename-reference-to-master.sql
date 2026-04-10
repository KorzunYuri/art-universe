-- Rename reference_id to master_id in all binding tables

-- 1. Rename in artist_binding
ALTER TABLE artist_binding 
    RENAME COLUMN reference_id TO master_id;

-- 2. Rename in album_binding
ALTER TABLE album_binding 
    RENAME COLUMN reference_id TO master_id;

-- 3. Rename in track_binding
ALTER TABLE track_binding 
    RENAME COLUMN reference_id TO master_id;

-- 4. Rename in category_binding
ALTER TABLE category_binding 
    RENAME COLUMN reference_id TO master_id;

-- 5. Rename in artist_category_binding
ALTER TABLE artist_category_binding 
    RENAME COLUMN reference_id TO master_id;

-- 6. Rename in artist_track_binding
ALTER TABLE artist_track_binding 
    RENAME COLUMN reference_id TO master_id;

-- Update constraint names to reflect the new column name

-- 1. Update constraint in album_binding
ALTER TABLE album_binding
    DROP CONSTRAINT IF EXISTS album_binding_FK_album;

ALTER TABLE album_binding
    ADD CONSTRAINT album_binding_FK_album
    FOREIGN KEY (master_id)
    REFERENCES album(id)
    ON DELETE CASCADE;

-- 2. Update constraint in track_binding
ALTER TABLE track_binding
    DROP CONSTRAINT IF EXISTS track_binding_FK_track;

ALTER TABLE track_binding
    ADD CONSTRAINT track_binding_FK_track
    FOREIGN KEY (master_id)
    REFERENCES track(id)
    ON DELETE CASCADE;

-- 3. Update constraint in artist_category_binding
ALTER TABLE artist_category_binding
    DROP CONSTRAINT IF EXISTS artist_category_FK_reference;

ALTER TABLE artist_category_binding
    ADD CONSTRAINT artist_category_FK_master
    FOREIGN KEY (master_id)
    REFERENCES artist_category(id)
    ON DELETE CASCADE;

-- 4. Update constraint in artist_track_binding
ALTER TABLE artist_track_binding
    DROP CONSTRAINT IF EXISTS artist_track_FK_reference;

ALTER TABLE artist_track_binding
    ADD CONSTRAINT artist_track_FK_master
    FOREIGN KEY (master_id)
    REFERENCES artist_track(id)
    ON DELETE CASCADE;

-- Update indexes to reflect the new column name

-- 1. Update index in artist_binding
DROP INDEX IF EXISTS artist_binding_I_reference_artist;
CREATE INDEX artist_binding_I_master_artist ON artist_binding (master_id);

-- 2. Update index in album_binding
DROP INDEX IF EXISTS album_binding_I_reference_album;
CREATE INDEX album_binding_I_master_album ON album_binding (master_id);

-- 3. Update index in track_binding
DROP INDEX IF EXISTS track_binding_I_reference_track;
CREATE INDEX track_binding_I_master_track ON track_binding (master_id);

-- 4. Update index in category_binding
DROP INDEX IF EXISTS category_binding_I_reference_category;
CREATE INDEX category_binding_I_master_category ON category_binding (master_id);

-- 5. Update index in artist_category_binding
DROP INDEX IF EXISTS idx_artist_category_binding_reference_id;
CREATE INDEX idx_artist_category_binding_master_id ON artist_category_binding (master_id);

-- 6. Update index in artist_track_binding
DROP INDEX IF EXISTS idx_artist_track_binding_reference_id;
CREATE INDEX idx_artist_track_binding_master_id ON artist_track_binding (master_id);
