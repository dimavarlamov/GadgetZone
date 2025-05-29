INSERT INTO users (name, password, email, role, balance, enabled) VALUES
('admin', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'admin@gadgetzone.com', 'ROLE_ADMIN', 1000000.00, TRUE),
('seller1', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'seller1@gadgetzone.com', 'ROLE_SELLER', 0.00, TRUE),
('buyer1', '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6', 'buyer1@gadgetzone.com', 'ROLE_BUYER', 100000.00, TRUE);

INSERT INTO categories (title) VALUES ('Smartphones'), ('Laptops'), ('Accessories');

INSERT INTO products (name, description, price, stock, category_id, seller_id, image_url) VALUES
('iPhone 15', 'Latest Apple smartphone', 49999.99, 100, 1, 2, '/images/iphone15.jpg');