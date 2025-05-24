from flask import Blueprint, render_template, request
from app.models.article import Article
from app import db

main_bp = Blueprint('main', __name__)

@main_bp.route('/')
def index():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 12, type=int)
    author = request.args.get('author', '')
    sort = request.args.get('sort', 'desc')
    
    query = Article.query
    
    if author:
        query = query.filter(Article.author_name == author)
    
    if sort == 'asc':
        query = query.order_by(Article.publish_time.asc())
    else:
        query = query.order_by(Article.publish_time.desc())
    
    pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    articles = pagination.items
    
    return render_template('index.html',
                         articles=articles,
                         pagination=pagination,
                         author=author,
                         sort=sort)

@main_bp.route('/article/<int:id>')
def article(id):
    article = Article.query.get_or_404(id)
    return render_template('article.html', article=article) 