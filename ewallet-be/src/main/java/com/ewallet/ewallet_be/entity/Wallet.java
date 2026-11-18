package com.ewallet.ewallet_be.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Setter
@Getter
@NoArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id",nullable = false,updatable = false)
    private Long userId;

    @Column(name = "balance",nullable = false,precision = 19,scale = 2)
    private BigDecimal balance;

    @Column(name = "currency",nullable = false,length = 3)
    private String currency;

    @Version
    private Long version;

    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at",nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate(){
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate(){
        updatedAt = LocalDateTime.now();
    }
}
