-- Add is_primary flag to artist table to distinguish primary artists from collaborations
-- Primary artists are those created via artist.getInfo API calls and represent the canonical version

ALTER TABLE artist ADD COLUMN is_primary BOOLEAN NOT NULL DEFAULT FALSE;

-- Add comment to explain the purpose of the column
COMMENT ON COLUMN artist.is_primary IS 'Indicates if this is a primary artist (from artist.getInfo) vs collaboration (from album.getInfo, etc.)';
