CREATE TABLE mu_raw_lastfm.entity_text_content (
        id                  BIGSERIAL   PRIMARY KEY
    ,   entity_type         SMALLINT    NOT NULL
    ,   entity_id           BIGINT      NOT NULL
    ,   content_type        SMALLINT    NOT NULL
    ,   content             TEXT        NOT NULL
    ,   published_at        VARCHAR(64)
    ,   api_call_id         BIGINT      NOT NULL REFERENCES mu_raw_lastfm.api_call(id)
    ,   data_snapshot_id    BIGINT      REFERENCES mu_raw_lastfm.data_snapshot(id)
    ,   created_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   updated_at          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
    ,   UNIQUE (entity_type, entity_id, content_type)
);

CREATE INDEX etc_entity_idx ON mu_raw_lastfm.entity_text_content(entity_type, entity_id);

COMMENT ON TABLE mu_raw_lastfm.entity_text_content IS 'Stores large text content (bios, wikis) separately from entity tables';
COMMENT ON COLUMN mu_raw_lastfm.entity_text_content.entity_type IS '1=ARTIST, 2=ALBUM, 3=TRACK, 4=TAG';
COMMENT ON COLUMN mu_raw_lastfm.entity_text_content.content_type IS '1=BIO_SUMMARY, 2=BIO_CONTENT, 3=WIKI_SUMMARY, 4=WIKI_CONTENT';
COMMENT ON COLUMN mu_raw_lastfm.entity_text_content.data_snapshot_id IS 'Used for freshness comparison during upserts';
