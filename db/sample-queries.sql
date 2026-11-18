-- Contoh native query MySQL 8 untuk wallet-service (ganti :param dengan parameter Spring)

-- 1. Mutasi dengan filter periode + paginasi
SELECT l.id, t.reference_no, t.type, l.direction, l.amount, l.balance_after, l.created_at, t.description
FROM ledger_entries l
JOIN transactions t ON t.id = l.transaction_id
WHERE l.wallet_id = :walletId
  AND l.created_at >= :startDate AND l.created_at < :endDate
ORDER BY l.created_at DESC, l.id DESC
LIMIT :size OFFSET :offset;

-- 2. Ringkasan bulanan + perbandingan dengan bulan sebelumnya (CTE + window function)
--    (MySQL tidak punya FILTER, jadi pakai SUM(CASE WHEN ...))
WITH monthly AS (
    SELECT DATE_FORMAT(created_at, '%Y-%m') AS period,
           SUM(CASE WHEN direction = 'CREDIT' THEN amount ELSE 0 END) AS total_in,
           SUM(CASE WHEN direction = 'DEBIT'  THEN amount ELSE 0 END) AS total_out
    FROM ledger_entries
    WHERE wallet_id = :walletId
    GROUP BY DATE_FORMAT(created_at, '%Y-%m')
)
SELECT period, total_in, total_out,
       total_in - total_out AS net,
       LAG(total_in - total_out) OVER (ORDER BY period) AS prev_net
FROM monthly
ORDER BY period DESC;

-- 3. Top 5 penerima transfer terbanyak (self-join + RANK)
SELECT cp.wallet_id    AS recipient_wallet_id,
       COUNT(*)        AS transfer_count,
       SUM(me.amount)  AS total_amount,
       RANK() OVER (ORDER BY COUNT(*) DESC) AS rnk
FROM ledger_entries me
JOIN transactions t    ON t.id = me.transaction_id
JOIN ledger_entries cp ON cp.transaction_id = me.transaction_id AND cp.wallet_id <> me.wallet_id
WHERE me.wallet_id = :walletId AND me.direction = 'DEBIT' AND t.type = 'TRANSFER'
GROUP BY cp.wallet_id
ORDER BY rnk
LIMIT 5;

-- 4. Rekonsiliasi: saldo wallet harus sama dengan hasil penjumlahan ledger (hasil kosong = konsisten)
SELECT w.id, w.balance,
       COALESCE(SUM(CASE WHEN l.direction = 'CREDIT' THEN l.amount ELSE -l.amount END), 0) AS ledger_balance
FROM wallets w
LEFT JOIN ledger_entries l ON l.wallet_id = w.id
GROUP BY w.id, w.balance
HAVING w.balance <> COALESCE(SUM(CASE WHEN l.direction = 'CREDIT' THEN l.amount ELSE -l.amount END), 0);
