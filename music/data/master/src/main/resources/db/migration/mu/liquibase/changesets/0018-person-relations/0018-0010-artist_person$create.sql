-- Create artist_person junction table (cross-domain: artist in mu, person in art)
CREATE TABLE IF NOT EXISTS artist_person (
    id BIGINT PRIMARY KEY,
    artist_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,  -- no FK to art.person, validated in app via art_view.v_person
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_artist_person_artist FOREIGN KEY (artist_id) REFERENCES artist(id) ON DELETE CASCADE,
    CONSTRAINT fk_artist_person_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id)
);

-- Create sequence for artist_person
CREATE SEQUENCE IF NOT EXISTS artist_person_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_artist_person_artist_id ON artist_person(artist_id);
CREATE INDEX IF NOT EXISTS idx_artist_person_person_id ON artist_person(person_id);
CREATE INDEX IF NOT EXISTS idx_artist_person_relation_type ON artist_person(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX IF NOT EXISTS uk_artist_person_untyped
    ON artist_person(artist_id, person_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_artist_person_typed
    ON artist_person(artist_id, person_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;
