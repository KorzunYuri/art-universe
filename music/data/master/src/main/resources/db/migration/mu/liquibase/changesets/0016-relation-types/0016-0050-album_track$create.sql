-- Create album_track junction table
CREATE TABLE album_track (
    id BIGINT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    track_id BIGINT NOT NULL,
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_track_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE,
    CONSTRAINT fk_album_track_track FOREIGN KEY (track_id) REFERENCES track(id) ON DELETE CASCADE,
    CONSTRAINT fk_album_track_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id)
);

-- Create sequence for album_track
CREATE SEQUENCE album_track_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX idx_album_track_album_id ON album_track(album_id);
CREATE INDEX idx_album_track_track_id ON album_track(track_id);
CREATE INDEX idx_album_track_relation_type ON album_track(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX uk_album_track_untyped
    ON album_track(album_id, track_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX uk_album_track_typed
    ON album_track(album_id, track_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;

-- Create album_track_binding table
CREATE TABLE album_track_binding (
    id BIGINT PRIMARY KEY,
    master_id BIGINT NOT NULL,
    data_source_id INTEGER NOT NULL,
    external_album_id BIGINT NOT NULL,
    external_track_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT album_track_FK_master
        FOREIGN KEY (master_id)
        REFERENCES album_track(id)
        ON DELETE CASCADE,
    CONSTRAINT album_track_UK_binding
        UNIQUE (data_source_id, external_album_id, external_track_id)
);

-- Create sequence for album_track_binding
CREATE SEQUENCE album_track_binding_seq INCREMENT BY 50 START WITH 1;

-- Create indexes for album_track_binding
CREATE INDEX idx_album_track_binding_master_id
    ON album_track_binding(master_id);

CREATE INDEX idx_album_track_binding_data_source
    ON album_track_binding(data_source_id);

CREATE INDEX idx_album_track_binding_external_album_id
    ON album_track_binding(external_album_id);

CREATE INDEX idx_album_track_binding_external_track_id
    ON album_track_binding(external_track_id);
