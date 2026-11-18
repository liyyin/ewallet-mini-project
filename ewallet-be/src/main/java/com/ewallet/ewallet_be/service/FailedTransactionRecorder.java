package com.ewallet.ewallet_be.service;

import com.ewallet.ewallet_be.entity.WalletTransaction;
import com.ewallet.ewallet_be.enums.TransactionStatus;
import com.ewallet.ewallet_be.enums.TransactionType;
import com.ewallet.ewallet_be.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class FailedTransactionRecorder {

    private final WalletTransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedTransfer(String referenceNo, BigDecimal amount, String description){
        WalletTransaction transaction = new WalletTransaction();
        transaction.setReferenceNo(referenceNo);
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setDescription(description);
        transactionRepository.save(transaction);
    }
}
