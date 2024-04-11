INSERT INTO users (id, email, password, first_name, last_name, is_deleted)
VALUES (2, 'user@example.com', 'hashedPassword', 'John', 'Doe', false);

INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);
