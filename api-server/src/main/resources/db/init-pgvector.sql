-- Enable pgvector extension for vector similarity search
-- This script runs automatically when PostgreSQL container starts for the first time

CREATE EXTENSION IF NOT EXISTS vector;

-- Verify extension is installed
SELECT extname, extversion FROM pg_extension WHERE extname = 'vector';

-- Create rag_vectors table for RAG (Retrieval-Augmented Generation)
CREATE TABLE IF NOT EXISTS rag_vectors (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(50) NOT NULL,
    source_id BIGINT NOT NULL,
    text_chunk TEXT NOT NULL,
    embedding vector(1536) NOT NULL,
    metadata JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX IF NOT EXISTS idx_rag_vectors_source ON rag_vectors(source_type, source_id);
CREATE INDEX IF NOT EXISTS idx_rag_vectors_created_at ON rag_vectors(created_at);

-- Create index for vector similarity search (HNSW index for better performance)
-- Note: This may take time if there's already data in the table
CREATE INDEX IF NOT EXISTS idx_rag_vectors_embedding ON rag_vectors USING hnsw (embedding vector_cosine_ops);
