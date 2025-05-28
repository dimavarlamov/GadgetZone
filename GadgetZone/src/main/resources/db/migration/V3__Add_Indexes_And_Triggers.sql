-- Добавляем составные индексы
CREATE INDEX idx_orders_user_status ON orders(user_id, status);
CREATE INDEX idx_products_category ON products(category_id);

-- Триггер для обновления баланса при создании заказа
DELIMITER $$
CREATE TRIGGER after_order_insert
AFTER INSERT ON orders
FOR EACH ROW
BEGIN
    UPDATE users
    SET balance = balance - NEW.total_amount
    WHERE id = NEW.user_id;

    UPDATE users u
    JOIN (
        SELECT p.seller_id, SUM(oi.price * oi.quantity) as total
        FROM order_items oi
        JOIN products p ON oi.product_id = p.id
        WHERE oi.order_id = NEW.id
        GROUP BY p.seller_id
    ) sales ON u.id = sales.seller_id
    SET u.balance = u.balance + sales.total;
END$$
DELIMITER ;