USE wallet_db;
SET time_zone = '+07:00';
-- ============ SCHEMA ============
CREATE TABLE wallets (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT        NOT NULL UNIQUE,   -- referensi logis ke user_db (tanpa FK lintas database)
    balance    DECIMAL(19,2) NOT NULL DEFAULT 0,
    currency   CHAR(3)       NOT NULL DEFAULT 'IDR',
    version    BIGINT        NOT NULL DEFAULT 0, -- optimistic locking (opsional)
    created_at DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_wallet_balance CHECK (balance >= 0)
);

CREATE TABLE transactions (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_no VARCHAR(64)   NOT NULL UNIQUE,  -- dipakai untuk idempotency
    type         VARCHAR(20)   NOT NULL,
    amount       DECIMAL(19,2) NOT NULL,
    status       VARCHAR(20)   NOT NULL,
    description  VARCHAR(255),
    created_at   DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT chk_trx_type   CHECK (type IN ('TOPUP', 'TRANSFER')),
    CONSTRAINT chk_trx_amount CHECK (amount > 0),
    CONSTRAINT chk_trx_status CHECK (status IN ('SUCCESS', 'FAILED'))
);

CREATE TABLE ledger_entries (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT        NOT NULL,
    wallet_id      BIGINT        NOT NULL,
    direction      VARCHAR(10)   NOT NULL,
    amount         DECIMAL(19,2) NOT NULL,
    balance_after  DECIMAL(19,2) NOT NULL,
    created_at     DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_ledger_transaction FOREIGN KEY (transaction_id) REFERENCES transactions (id),
    CONSTRAINT fk_ledger_wallet      FOREIGN KEY (wallet_id)      REFERENCES wallets (id),
    CONSTRAINT chk_ledger_direction  CHECK (direction IN ('DEBIT', 'CREDIT')),
    CONSTRAINT chk_ledger_amount     CHECK (amount > 0),
    INDEX idx_ledger_wallet_created (wallet_id, created_at DESC)
);

-- ============ SEED DATA ============
-- 5 wallet (user_id 1..5), lalu simulasi 90 hari: tiap hari 1 top up + (jika saldo cukup) 1 transfer.
-- Diproses berurutan per hari sehingga balance_after selalu konsisten dengan saldo akhir.
INSERT INTO wallets (user_id) VALUES (1), (2), (3), (4), (5);

DELIMITER //

CREATE PROCEDURE seed_wallet_data()
BEGIN
    DECLARE d   INT DEFAULT 90;
    DECLARE w1  BIGINT;
    DECLARE w2  BIGINT;
    DECLARE amt DECIMAL(19,2);
    DECLARE ts  DATETIME(6);
    DECLARE tx  BIGINT;
    DECLARE b1  DECIMAL(19,2);
    DECLARE b2  DECIMAL(19,2);

    WHILE d >= 1 DO
        SET ts = NOW() - INTERVAL d DAY;

        -- Top up
        SET w1  = 1 + FLOOR(RAND() * 5);
        SET amt = (1 + FLOOR(RAND() * 5)) * 200000;
        UPDATE wallets SET balance = balance + amt, updated_at = ts WHERE id = w1;
        SELECT balance INTO b1 FROM wallets WHERE id = w1;
        INSERT INTO transactions (reference_no, type, amount, status, description, created_at)
        VALUES (CONCAT('SEED-TOPUP-', d), 'TOPUP', amt, 'SUCCESS', 'Top up saldo', ts);
        SET tx = LAST_INSERT_ID();
        INSERT INTO ledger_entries (transaction_id, wallet_id, direction, amount, balance_after, created_at)
        VALUES (tx, w1, 'CREDIT', amt, b1, ts);

        -- Transfer (pengirim dan penerima selalu berbeda)
        SET w1  = 1 + FLOOR(RAND() * 5);
        SET w2  = MOD(w1 + FLOOR(RAND() * 4), 5) + 1;
        SET amt = (1 + FLOOR(RAND() * 5)) * 50000;
        SELECT balance INTO b1 FROM wallets WHERE id = w1;

        IF b1 >= amt THEN
            UPDATE wallets SET balance = balance - amt, updated_at = ts WHERE id = w1;
            UPDATE wallets SET balance = balance + amt, updated_at = ts WHERE id = w2;
            SELECT balance INTO b1 FROM wallets WHERE id = w1;
            SELECT balance INTO b2 FROM wallets WHERE id = w2;
            INSERT INTO transactions (reference_no, type, amount, status, description, created_at)
            VALUES (CONCAT('SEED-TRF-', d), 'TRANSFER', amt, 'SUCCESS', 'Transfer antar wallet', ts);
            SET tx = LAST_INSERT_ID();
            INSERT INTO ledger_entries (transaction_id, wallet_id, direction, amount, balance_after, created_at)
            VALUES (tx, w1, 'DEBIT',  amt, b1, ts),
                   (tx, w2, 'CREDIT', amt, b2, ts);
        END IF;

        SET d = d - 1;
    END WHILE;
END//

DELIMITER ;

CALL seed_wallet_data();
DROP PROCEDURE seed_wallet_data;
