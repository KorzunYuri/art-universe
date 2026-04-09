CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX artist_name_trgm_idx ON artist USING gin (lower(name) gin_trgm_ops);
CREATE INDEX album_name_trgm_idx  ON album USING gin (lower(name) gin_trgm_ops);
CREATE INDEX track_name_trgm_idx  ON track USING gin (lower(name) gin_trgm_ops);
CREATE INDEX category_name_trgm_idx ON category USING gin (lower(name) gin_trgm_ops);
