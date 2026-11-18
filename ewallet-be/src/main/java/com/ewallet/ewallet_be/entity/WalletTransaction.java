package com.ewallet.ewallet_be.entity;

import com.ewallet.ewallet_be.enums.TransactionStatus;
import com.ewallet.ewallet_be.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import javax.print.DocFlavor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_no",nullable = false, updatable = false)
    private String referenceNo;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "type",nullable =false,length = 20)
    private TransactionType type;

    @Column(name = "amount",nullable = false,precision = 19,scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status",nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
    }

}
