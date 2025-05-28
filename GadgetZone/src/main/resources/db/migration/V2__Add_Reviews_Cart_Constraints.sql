-- Добавляем таблицу отзывов
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_review (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Создаем таблицу корзины
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    UNIQUE KEY unique_cart_item (user_id, product_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- Добавляем индексы для оптимизации
CREATE INDEX idx_reviews_product ON reviews(product_id);
CREATE INDEX idx_cart_user ON cart_items(user_id);

-- Обновляем тестовые данные
INSERT INTO users (name, password, email, role, balance, enabled) VALUES
('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'admin@gadgetzone.com', 'ADMIN', 10000.00, TRUE),
('seller1', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'seller1@gadgetzone.com', 'SELLER', 0.00, TRUE),
('buyer1', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'buyer1@gadgetzone.com', 'BUYER', 1000.00, TRUE);

-- Добавляем пример категории
INSERT INTO categories (title) VALUES ('Smartphones'), ('Laptops'), ('Accessories');

-- Добавляем пример товара
INSERT INTO products (name, description, price, stock, category_id, seller_id, image_url) VALUES
('iPhone 15', 'Latest Apple smartphone', 999.99, 100, 1, 2, '/images/iphone15.jpg');