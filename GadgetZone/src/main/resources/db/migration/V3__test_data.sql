INSERT INTO categories (name) VALUES ('Смартфоны'), ('Ноутбуки');

INSERT INTO products (name, description, price, stock, category_id, seller_id, image_url) 
VALUES 
    ('iPhone 15', 'Описание iPhone 15', 999.99, 10, 1, 1, '/images/iphone15.jpg'),
    ('MacBook Pro', 'Описание MacBook Pro', 1999.99, 5, 2, 1, '/images/macbook.jpg');