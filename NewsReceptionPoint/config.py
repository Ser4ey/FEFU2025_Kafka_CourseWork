from pydantic_settings import BaseSettings
from pydantic import Field

class Settings(BaseSettings):
    # Telegram
    BOT_TOKEN: str = Field(..., env="BOT_TOKEN")
    
    # Database
    DATABASE_URL: str = Field(..., env="DATABASE_URL")
    
    # Kafka
    KAFKA_BOOTSTRAP_SERVERS: str = Field(..., env="KAFKA_BOOTSTRAP_SERVERS")
    KAFKA_NEWS_TO_REVIEW_TOPIC: str = Field("news-to-review", env="KAFKA_NEWS_TO_REVIEW_TOPIC")
    KAFKA_NEWS_REVIEWED_TOPIC: str = Field("news-reviewed", env="KAFKA_NEWS_REVIEWED_TOPIC")
    
    class Config:
        env_file = ".env"

settings = Settings()

