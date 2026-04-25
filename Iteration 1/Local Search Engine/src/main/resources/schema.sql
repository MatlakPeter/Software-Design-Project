DROP TABLE IF EXISTS files CASCADE;

CREATE TABLE files (
    id SERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    filepath TEXT UNIQUE NOT NULL,
    content TEXT,
    last_modified BIGINT
);

-- Enable the trigram extension for partial filename matching
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- Create Indexes for fast searching
-- GIN index for full-text content search (using 'simple' for multi-language support)
CREATE INDEX content_idx ON files USING GIN (to_tsvector('simple', content));

-- GIN index for filename trigram search
CREATE INDEX filename_trgm_idx ON files USING GIN (filename gin_trgm_ops);

-- For Query Suggestion
CREATE TABLE search_history (
    query VARCHAR(255) PRIMARY KEY,
    search_count INT NOT NULL DEFAULT 1
);