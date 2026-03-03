-- Create track_person junction table (cross-domain: track in mu, person in art)
CREATE TABLE IF NOT EXISTS track_person (
    id BIGINT PRIMARY KEY,
    track_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,  -- no FK to art.person, validated in app via art_view.v_person
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_track_person_track FOREIGN KEY (track_id) REFERENCES track(id) ON DELETE CASCADE,
    CONSTRAINT fk_track_person_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id)
);

-- Create sequence for track_person
CREATE SEQUENCE IF NOT EXISTS track_person_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_track_person_track_id ON track_person(track_id);
CREATE INDEX IF NOT EXISTS idx_track_person_person_id ON track_person(person_id);
CREATE INDEX IF NOT EXISTS idx_track_person_relation_type ON track_person(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX IF NOT EXISTS uk_track_person_untyped
    ON track_person(track_id, person_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_track_person_typed
    ON track_person(track_id, person_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;
