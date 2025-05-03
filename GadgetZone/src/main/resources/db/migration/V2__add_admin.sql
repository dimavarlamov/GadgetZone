INSERT INTO `users` (`name`, `email`, `password_hash`, `role`, `balance`)
VALUES (
    'Администратор', 
    'admin@gmail.com', 
    '$2a$12$4VWr5o7c6XZ5qy1gD7zR0e.9nTd0jKb1Lk8mZf3vYhGJpNlQ2sS3u', 
    'ADMIN', 
    0.00
)
ON DUPLICATE KEY UPDATE email = email; 