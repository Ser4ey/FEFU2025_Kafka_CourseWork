CREATE TABLE IF NOT EXISTS articles (
    id SERIAL PRIMARY KEY,
    news_id VARCHAR(50) UNIQUE,
    title VARCHAR(500),
    content TEXT,
    author_name VARCHAR(255),
    publish_time TIMESTAMP NOT NULL
); 
