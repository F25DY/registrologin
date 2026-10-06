ALTER TABLE users
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

INSERT INTO users (email, password, first_name, last_name, role)
VALUES (
    'admin@registrologin.com',
    '$2a$10$HLBxxSpwkHxEyCDcvHfwwenWnP28aFqtfUR0N7BbuiHlWxhP0lSI.',
    'Administrador',
    'Sistema',
    'ADMIN'
)
ON DUPLICATE KEY UPDATE
    password = '$2a$10$HLBxxSpwkHxEyCDcvHfwwenWnP28aFqtfUR0N7BbuiHlWxhP0lSI.',
    first_name = 'Administrador',
    last_name = 'Sistema',
    role = 'ADMIN';
