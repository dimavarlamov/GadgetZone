INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

INSERT INTO users (username, password, email, first_name, last_name, enabled, role)
VALUES (
    'admin',
    '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6',
    'admin@gadgetzone.com',
    'Admin',
    'User',
    1,
    'ADMIN'
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

INSERT INTO users (username, password, email, first_name, last_name, enabled, role)
VALUES (
    'test',
    '$2a$10$slYQmyNdGzTn7ZLBXBChFOC9f6kFjAqPhccnP6DxlWXx2lPk1C3G6',
    'test@gadgetzone.com',
    'Test',
    'User',
    1,
    'USER'
);

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'test' AND r.name = 'ROLE_USER';
