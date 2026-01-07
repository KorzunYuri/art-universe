-- Fix sequence allocation sizes to match entity allocationSize values
-- artist_seq, track_seq, game_seq, generation_seq: change from 50 to 1
-- generation_track_seq: keep 50 (already matches)

ALTER SEQUENCE artist_seq INCREMENT BY 1;
ALTER SEQUENCE track_seq INCREMENT BY 1;
ALTER SEQUENCE game_seq INCREMENT BY 1;
ALTER SEQUENCE generation_seq INCREMENT BY 1;
