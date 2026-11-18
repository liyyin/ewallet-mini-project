package com.ewallet.ewallet_be.repository;

import com.ewallet.ewallet_be.entity.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction,Long> {

    Optional<WalletTransaction> findByReferenceNo(String referenceNo);
}
