-- Create view for track-category mapping through artists with inheritance
CREATE OR REPLACE VIEW mu_quiz.mu_v_track_category AS
WITH category_hierarchy AS (
    -- Direct categories
    SELECT 
        vac.artist_id,
        vac.category_id
    FROM mu_view.v_artist_category vac
    
    UNION
    
    -- Inherited categories (children)
    SELECT 
        vac.artist_id,
        vcc.child_id as category_id
    FROM mu_view.v_artist_category vac
    JOIN mu_view.v_category_children vcc ON vac.category_id = vcc.id
)
SELECT 
    vt.id as track_id,
    ch.category_id
FROM mu_view.v_track vt
JOIN category_hierarchy ch ON vt.primary_artist_id = ch.artist_id;
