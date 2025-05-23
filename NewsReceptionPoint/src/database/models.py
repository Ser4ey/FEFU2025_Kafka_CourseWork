from sqlalchemy import Column, Integer, String, BigInteger
from sqlalchemy.orm import DeclarativeBase

class Base(DeclarativeBase):
    pass

class Correspondent(Base):
    __tablename__ = "correspondents"
    
    id = Column(Integer, primary_key=True)
    telegram_id = Column(BigInteger, unique=True)
    username = Column(String(255)) 