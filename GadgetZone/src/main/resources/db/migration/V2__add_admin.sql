INSERT INTO users (id, username, password, email, name, role)
VALUES (1, 'admin', '$2a$10$XlQY6W7U3bZkKj4p8z6n3uJd7rV2mW1oGt0YhLq9sBvNcRtPfKlS', 'admin@mail.ru', 'Admin', 'ADMIN');

ALTER SEQUENCE user_seq RESTART WITH 2;