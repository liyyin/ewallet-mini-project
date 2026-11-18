package com.ewallet.ewallet_be.repository;

import com.ewallet.ewallet_be.entity.LedgerEntry;
import com.ewallet.ewallet_be.repository.projection.MonthlySummaryView;
import com.ewallet.ewallet_be.repository.projection.MutationView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry,Long> {

    @Query(value = """
    SELECT l.id AS id,
        t.reference_no AS referenceNo,
        t.type AS type,
        l.direction AS direction,
        l.amount AS amount,
        l.balance_after AS balanceAfter,
        l.created_at AS createdAt,
        t.description AS description
    FROM ledger_entries l
    JOIN transactions t on t.id = l.transaction_id
    WHERE l.wallet_id = :walletId
        AND l.created_at >= :startAt AND l.created_at < :endAt
    ORDER BY l.created_at DESC, l.id DESC
    LIMIT :limit""",nativeQuery = true)
    List<MutationView> findMutations(@Param("walletId") Long walletId,
                                     @Param("startAt") LocalDateTime startAt,
                                     @Param("endAt") LocalDateTime endAt,
                                     @Param("limit") int limit);

    @Query(value = """
    WITH monthly AS (
        SELECT DATE_FORMAT(created_at, '%Y-%m') AS month_key,
            SUM(CASE WHEN direction = 'CREDIT' THEN amount ELSE 0 END) AS total_in,
            SUM(CASE WHEN direction = 'DEBIT' THEN amount ELSE 0 END) AS total_out,
            COUNT(*) AS entry_count
        FROM ledger_entries
        WHERE wallet_id = :walletId
        GROUP BY DATE_FORMAT(created_at,'%Y-%m')
        )
    SELECT month_key AS periodMonth,
        total_in AS totalIn,
        total_out AS totalOut,
        total_in - total_out AS net,
        LAG(total_in -total_out) OVER (ORDER BY month_key) AS previousNet,
        entry_count AS entryCount
    FROM monthly
    ORDER BY month_key DESC
    """,nativeQuery = true)
    List<MonthlySummaryView> findMonthlySummary(@Param("walletId") Long walletId);
}
