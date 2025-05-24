from datetime import datetime
from app import db

class Article(db.Model):
    __tablename__ = 'articles'
    
    id = db.Column(db.Integer, primary_key=True)
    news_id = db.Column(db.String(50), unique=True)
    title = db.Column(db.String(500))
    content = db.Column(db.Text)
    author_name = db.Column(db.String(255))
    publish_time = db.Column(db.DateTime, nullable=False)
    
    def __repr__(self):
        return f'<Article {self.title}>' 