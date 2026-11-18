package com.ewallet.ewallet_be.entity;

import com.ewallet.ewallet_be.enums.Direction;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id",nullable = false)
    private Long transactionId;

    @Column(name = "wallet_id",nullable = false)
    private Long walletId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "direction", length = 10)
    private Direction direction;

    @Column(name = "amount",nullable = false,precision = 19,scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after",nullable = false,precision = 19,scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
    }
}
