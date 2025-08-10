DELETE
FROM artist_binding
WHERE master_id IN (
    SELECT
        master_id
    FROM
        artist_binding b
    LEFT JOIN
        artist m
            ON b.master_id = m.id
    WHERE m.id IS NULL
);

-- Add FK constraint for artist_binding -> artist
ALTER TABLE artist_binding 
ADD CONSTRAINT artist_binding_fk_artist 
FOREIGN KEY (master_id) REFERENCES artist(id) ON DELETE CASCADE;

DELETE
FROM category_binding
WHERE master_id IN (
    SELECT
        master_id
    FROM
        category_binding b
    LEFT JOIN
        category m
            ON b.master_id = m.id
    WHERE m.id IS NULL
);

-- Add FK constraint for category_binding -> category
ALTER TABLE category_binding 
ADD CONSTRAINT category_binding_fk_category 
FOREIGN KEY (master_id) REFERENCES category(id) ON DELETE CASCADE;
