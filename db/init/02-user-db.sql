USE user_db;
SET time_zone = '+07:00';
CREATE TABLE users (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    phone      VARCHAR(20)  NOT NULL UNIQUE,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_users_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'BLOCKED'))
);

INSERT INTO users (full_name, email, phone) VALUES
    ('Andi Pratama',   'andi@example.com',  '081200000001'),
    ('Budi Santoso',   'budi@example.com',  '081200000002'),
    ('Citra Lestari',  'citra@example.com', '081200000003'),
    ('Dewi Anggraini', 'dewi@example.com',  '081200000004'),
    ('Eko Wijaya',     'eko@example.com',   '081200000005');
