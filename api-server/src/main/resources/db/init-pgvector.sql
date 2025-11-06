-- Enable pgvector extension for vector similarity search
-- This script runs automatically when PostgreSQL container starts for the first time

CREATE EXTENSION IF NOT EXISTS vector;

-- Verify extension is installed
SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';
