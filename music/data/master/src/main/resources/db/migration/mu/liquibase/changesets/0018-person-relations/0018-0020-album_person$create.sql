-- Create album_person junction table (cross-domain: album in mu, person in art)
CREATE TABLE IF NOT EXISTS album_person (
    id BIGINT PRIMARY KEY,
    album_id BIGINT NOT NULL,
    person_id BIGINT NOT NULL,  -- no FK to art.person, validated in app via art_view.v_person
    relation_type_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_album_person_album FOREIGN KEY (album_id) REFERENCES album(id) ON DELETE CASCADE,
    CONSTRAINT fk_album_person_relation_type FOREIGN KEY (relation_type_id) REFERENCES relation_type(id)
);

-- Create sequence for album_person
CREATE SEQUENCE IF NOT EXISTS album_person_seq INCREMENT BY 50 START WITH 1;

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_album_person_album_id ON album_person(album_id);
CREATE INDEX IF NOT EXISTS idx_album_person_person_id ON album_person(person_id);
CREATE INDEX IF NOT EXISTS idx_album_person_relation_type ON album_person(relation_type_id);

-- Partial unique indexes
CREATE UNIQUE INDEX IF NOT EXISTS uk_album_person_untyped
    ON album_person(album_id, person_id)
    WHERE relation_type_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_album_person_typed
    ON album_person(album_id, person_id, relation_type_id)
    WHERE relation_type_id IS NOT NULL;
