-- Create artist_album junction table
CREATE TABLE IF NOT EXISTS artist_album (
    id BIGINT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    album_id BIGINT NOT NULL,
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artist_album_artist FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist_album_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist_album_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id)
);

-- Create sequence for artist_album
CREATE SEQUENCE IF NOT EXISTS artist_album_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_artist_album_artist_id ON artist_album(artist_id);
CREATE INDEX IF NOT EXISTS idx_artist_album_album_id ON artist_album(album_id);
CREATE INDEX IF NOT EXISTS idx_artist_album_relation_type ON artist_album(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX IF NOT EXISTS uk_artist_album_untyped
    ON artist_album(artist_id, album_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_artist_album_typed
    ON artist_album(artist_id, album_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;

-- Create artist_album_binding table
CREATE TABLE IF NOT EXISTS artist_album_binding (
    id BIGINT PRIMARY KEY,
    master_id BIGINT NOT NULL,
    data_source_id INTEGER NOT NULL,
    external_artist_id BIGINT NOT NULL,
    external_album_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT artist_album_FK_master
        FOREIGN KEY (master_id)
        REFERENCES artist_album(id)
        ON DELETE CASCADE,
    CONSTRAINT artist_album_UK_binding
        UNIQUE (data_source_id, external_artist_id, external_album_id)
);

-- Create sequence for artist_album_binding
CREATE SEQUENCE IF NOT EXISTS artist_album_binding_seq INCREMENT BY 50 START WITH 1;

-- Create indexes for artist_album_binding
CREATE INDEX IF NOT EXISTS idx_artist_album_binding_master_id
    ON artist_album_binding(master_id);

CREATE INDEX IF NOT EXISTS idx_artist_album_binding_data_source
    ON artist_album_binding(data_source_id);

CREATE INDEX IF NOT EXISTS idx_artist_album_binding_external_artist_id
    ON artist_album_binding(external_artist_id);

CREATE INDEX IF NOT EXISTS idx_artist_album_binding_external_album_id
    ON artist_album_binding(external_album_id);
