-- Test data for BindingService basic functionality tests

-- Insert test artists
INSERT INTO artist (id, name, created_at, updated_at) VALUES 
(101, 'Test Artist 1', NOW(), NOW()),
(102, 'Test Artist 2', NOW(), NOW()),
(103, 'Test Artist 3', NOW(), NOW());

-- Insert test categories
INSERT INTO category (id, name, created_at, updated_at) VALUES 
(201, 'Test Category 1', NOW(), NOW()),
(202, 'Test Category 2', NOW(), NOW()),
(203, 'Test Category 3', NOW(), NOW());

-- Insert test tracks
INSERT INTO track (id, name, primary_artist_id, created_at, updated_at) VALUES 
(301, 'Test Track 1', 101, NOW(), NOW()),
(302, 'Test Track 2', 102, NOW(), NOW()),
(303, 'Test Track 3', 103, NOW(), NOW());

-- Insert artist bindings (only 1001 and 1002, not 1003)
INSERT INTO artist_binding (id, master_id, data_source_id, external_id, created_at, updated_at) VALUES 
(1, 101, 1, 1001, NOW(), NOW()),
(2, 102, 1, 1002, NOW(), NOW());

-- Insert category bindings (only 2001 and 2002, not 2999)
INSERT INTO category_binding (id, master_id, data_source_id, external_id, created_at, updated_at) VALUES 
(1, 201, 1, 2001, NOW(), NOW()),
(2, 202, 1, 2002, NOW(), NOW());

-- Insert track bindings (both 3001 and 3002)
INSERT INTO track_binding (id, master_id, data_source_id, external_id, created_at, updated_at) VALUES 
(1, 301, 1, 3001, NOW(), NOW()),
(2, 302, 1, 3002, NOW(), NOW());
