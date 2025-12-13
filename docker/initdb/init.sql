CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE chunks (
                        id SERIAL PRIMARY KEY,
                        content TEXT,
                        embedding VECTOR(1536)
);
