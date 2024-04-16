INSERT INTO users (id, email, password, first_name, last_name, is_deleted)
VALUES (1, 'admin@example.com', 'hashedPassword', 'Admin', 'Admin', false),
       (2, 'user@example.com', 'hashedPassword', 'John', 'Doe', false),
       (3, 'user2@example.com', 'hashedPassword', 'John2', 'Doe2', false);

INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);
