-- Create index on api_response.created_at for efficient deletion of old responses
-- This index will optimize queries like: DELETE FROM api_response WHERE created_at < NOW() - INTERVAL '1 month'

CREATE INDEX IF NOT EXISTS api_response_i_created_at
ON api_response (created_at);

-- Add comment to explain the purpose
COMMENT ON INDEX api_response_i_created_at IS 'Optimizes deletion of old api_response records by created_at timestamp';
