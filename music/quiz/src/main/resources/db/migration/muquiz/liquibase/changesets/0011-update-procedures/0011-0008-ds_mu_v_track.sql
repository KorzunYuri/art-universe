CREATE OR REPLACE VIEW mu_quiz.ds_mu_v_track
AS
SELECT
        vt.id                       as track_id
    ,   vt.name                     as name
    ,   vt.primary_artist_id        as primary_artist_id
FROM
    mu_view.v_track vt;