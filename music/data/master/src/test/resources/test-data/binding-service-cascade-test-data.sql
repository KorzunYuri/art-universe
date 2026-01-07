-- Test data for BindingService cascade delete tests

-- Insert test artists
INSERT INTO artist (id, name, created_at, updated_at) VALUES 
(101, 'Test Artist 1', NOW(), NOW()),
(102, 'Test Artist 2', NOW(), NOW());

-- Insert test categories
INSERT INTO category (id, name, created_at, updated_at) VALUES 
(201, 'Test Category 1', NOW(), NOW()),
(202, 'Test Category 2', NOW(), NOW());

-- Insert test tracks
INSERT INTO track (id, name, primary_artist_id, created_at, updated_at) VALUES 
(301, 'Test Track 1', 101, NOW(), NOW()),
(302, 'Test Track 2', 102, NOW(), NOW());

-- Insert artist bindings
INSERT INTO artist_binding (id, master_id, data_source_id, external_id, created_at, updated_at) VALUES 
(1, 101, 1, 1001, NOW(), NOW()),
(2, 102, 1, 1002, NOW(), NOW());

-- Insert category bindings
INSERT INTO category_binding (id, master_id, data_source_id, external_id, created_at, updated_at) VALUES 
(1, 201, 1, 2001, NOW(), NOW()),
(2, 202, 1, 2002, NOW(), NOW());

-- Insert track bindings
INSERT INTO track_binding (id, master_id, data_source_id, external_id, created_at, updated_at) VALUES 
(1, 301, 1, 3001, NOW(), NOW()),
(2, 302, 1, 3002, NOW(), NOW());

-- Insert artist-category relations
INSERT INTO artist_category (id, artist_id, category_id, created_at, updated_at) VALUES 
(1, 101, 201, NOW(), NOW()),
(2, 102, 202, NOW(), NOW());

-- Insert artist-track relations
INSERT INTO artist_track (id, artist_id, track_id, created_at, updated_at) VALUES 
(1, 101, 301, NOW(), NOW()),
(2, 102, 302, NOW(), NOW());

-- Insert artist-category relation bindings
INSERT INTO artist_category_binding (id, master_id, data_source_id, external_artist_id, external_category_id, created_at, updated_at) VALUES 
(1, 1, 1, 1001, 2001, NOW(), NOW()),
(2, 2, 1, 1002, 2002, NOW(), NOW());

-- Insert artist-track relation bindings
INSERT INTO artist_track_binding (id, master_id, data_source_id, external_artist_id, external_track_id, created_at, updated_at) VALUES 
(1, 1, 1, 1001, 3001, NOW(), NOW()),
(2, 2, 1, 1002, 3002, NOW(), NOW());
