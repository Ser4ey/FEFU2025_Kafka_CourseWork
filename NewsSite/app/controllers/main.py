from flask import Blueprint, render_template, request
from app.models.article import Article
from app import db
from sqlalchemy import distinct

main_bp = Blueprint('main', __name__)

@main_bp.route('/')
def index():
    page = request.args.get('page', 1, type=int)
    per_page = request.args.get('per_page', 12, type=int)
    selected_authors = request.args.getlist('authors')
    sort = request.args.get('sort', 'desc')
    
    # Получаем список всех авторов
    all_authors = db.session.query(distinct(Article.author_name)).order_by(Article.author_name).all()
    all_authors = [author[0] for author in all_authors]
    
    query = Article.query
    
    if selected_authors:
        query = query.filter(Article.author_name.in_(selected_authors))
    
    if sort == 'asc':
        query = query.order_by(Article.publish_time.asc())
    else:
        query = query.order_by(Article.publish_time.desc())
    
    pagination = query.paginate(page=page, per_page=per_page, error_out=False)
    articles = pagination.items
    
    return render_template('index.html',
                         articles=articles,
                         pagination=pagination,
                         selected_authors=selected_authors,
                         all_authors=all_authors,
                         sort=sort)

@main_bp.route('/article/<int:id>')
def article(id):
    article = Article.query.get_or_404(id)
    return render_template('article.html', article=article)