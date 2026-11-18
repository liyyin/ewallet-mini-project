package com.ewallet.ewallet_be.service;

import com.ewallet.ewallet_be.client.UserClient;
import com.ewallet.ewallet_be.dto.*;
import com.ewallet.ewallet_be.entity.LedgerEntry;
import com.ewallet.ewallet_be.entity.Wallet;
import com.ewallet.ewallet_be.entity.WalletTransaction;
import com.ewallet.ewallet_be.enums.Direction;
import com.ewallet.ewallet_be.enums.TransactionStatus;
import com.ewallet.ewallet_be.enums.TransactionType;
import com.ewallet.ewallet_be.exception.BusinessException;
import com.ewallet.ewallet_be.repository.LedgerEntryRepository;
import com.ewallet.ewallet_be.repository.WalletRepository;
import com.ewallet.ewallet_be.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final FailedTransactionRecorder failedTransactionRecorder;
    private final UserClient userClient;

    public BalanceResponse getBalance(Long walletId){
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND,"Wallet Not Found"));
        return new BalanceResponse(wallet.getId(),wallet.getUserId(),wallet.getBalance(),wallet.getCurrency());
    }

    @Transactional
    public TransactionResponse topUp(Long walletId, TopUpRequest request){
        if (transactionRepository.findByReferenceNo(request.referenceNo()).isPresent()){
            throw new BusinessException(HttpStatus.CONFLICT,"Reference Number already used");
        }

        Wallet wallet = lockWallet(walletId);

        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal newBalance = wallet.getBalance().add(request.amount());
        wallet.setBalance(newBalance);

        WalletTransaction transaction = new WalletTransaction();
        transaction.setReferenceNo(request.referenceNo());
        transaction.setType(TransactionType.TOPUP);
        transaction.setAmount(request.amount());
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription("Top Up Saldo");
        transaction = transactionRepository.save(transaction);

        LedgerEntry entry = newLedgerEntry(transaction.getId(),wallet.getId(),Direction.CREDIT,request.amount(),newBalance);
        ledgerEntryRepository.save(entry);

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReferenceNo(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                balanceBefore,
                newBalance,
                transaction.getCreatedAt()
        );
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request){
        Long fromId = request.fromWalletId();
        Long toId = request.toWalletId();
        BigDecimal amount = request.amount();

        if(fromId.equals(toId)){
            throw new BusinessException(HttpStatus.BAD_REQUEST,"Cannot Transfer to the same wallet");
        }

        if (transactionRepository.findByReferenceNo(request.referenceNo()).isPresent()){
            throw new BusinessException(HttpStatus.CONFLICT,"Reference Number already used");
        }

        Wallet source;
        Wallet destination;

        if (fromId < toId){
            source = lockWallet(fromId);
            destination = lockWallet(toId);
        }else{
            destination = lockWallet(toId);
            source = lockWallet(fromId);
        }

        if(source.getBalance().compareTo(amount) < 0){
            failedTransactionRecorder.recordFailedTransfer(
                    request.referenceNo(),amount,"Insufficient balance: wallet " + fromId + " to wallet " + toId
            );
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY,"Insufficient balance");
        }

        BigDecimal oldBalance = source.getBalance();
        source.setBalance(source.getBalance().subtract(amount));
        destination.setBalance(destination.getBalance().add(amount));

        WalletTransaction transaction = new WalletTransaction();
        transaction.setReferenceNo(request.referenceNo());
        transaction.setType(TransactionType.TRANSFER);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(request.description());
        transaction = transactionRepository.save(transaction);

        ledgerEntryRepository.save(newLedgerEntry(transaction.getId(),source.getId(),Direction.DEBIT,amount,source.getBalance()));
        ledgerEntryRepository.save(newLedgerEntry(transaction.getId(),destination.getId(),Direction.CREDIT,amount,destination.getBalance()));

        return new TransactionResponse(
                transaction.getId(),
                transaction.getReferenceNo(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getStatus(),
                oldBalance,
                source.getBalance(),
                transaction.getCreatedAt(),
                destination.getId()
        );
    }

    private Wallet lockWallet(Long id){
        return walletRepository.findByIdForUpdate(id)
                .orElseThrow(()-> new BusinessException(HttpStatus.NOT_FOUND,"Wallet "+ id + " not found"));
    }

    private LedgerEntry newLedgerEntry(Long transactionId,Long walletId, Direction direction, BigDecimal amount, BigDecimal balanceAfter){
        LedgerEntry entry = new LedgerEntry();
        entry.setTransactionId(transactionId);
        entry.setWalletId(walletId);
        entry.setDirection(direction);
        entry.setAmount(amount);
        entry.setBalanceAfter(balanceAfter);
        return entry;
    }

    public List<MutationResponse> getMutations(Long walletId, LocalDate from, LocalDate to, int limit){
        if (!walletRepository.existsById(walletId)){
            throw new BusinessException(HttpStatus.NOT_FOUND,"Wallet "+ walletId +" not found");
        }
        LocalDate end =  (to != null) ? to : LocalDate.now();
        LocalDate start = (from != null) ? from : end.minusDays(29);

        if(start.isAfter(end)){
            throw new BusinessException(HttpStatus.BAD_REQUEST,"start date mus not after end date");
        }

        LocalDateTime startAt = start.atStartOfDay();
        LocalDateTime endAt = end.plusDays(1).atStartOfDay();

        return ledgerEntryRepository.findMutations(walletId,startAt,endAt,Math.min(Math.max(limit, 1), 100))
                .stream()
                .map(v -> new MutationResponse(v.getId(),v.getReferenceNo(),v.getType(),v.getDirection(),
                        v.getAmount(),v.getBalanceAfter(),v.getCreatedAt(),v.getDescription()))
                .toList();
    }

    public WalletSummaryResponse getSummary(Long walletId){
        if(!walletRepository.existsById(walletId)){
            throw new BusinessException(HttpStatus.NOT_FOUND,"Wallet " + walletId + " not found");
        }

        List<MonthlySummaryItem> months = ledgerEntryRepository.findMonthlySummary(walletId)
                .stream()
                .map(v -> new MonthlySummaryItem(v.getPeriodMonth(),v.getTotalIn(),v.getTotalOut(),
                        v.getNet(),v.getPreviousNet(),v.getEntryCount()))
                .toList();

        BigDecimal grandIn = months.stream()
                .map(MonthlySummaryItem::totalIn)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal grandOut = months.stream()
                .map(MonthlySummaryItem::totalOut)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        return new WalletSummaryResponse(
                walletId,
                grandIn,
                grandOut,
                grandIn.subtract(grandOut),
                months
        );
    }

    public BalanceResponse createWallet(CreateWalletRequest request){
        Long userId = request.id();

        if(walletRepository.existsById(userId)){
            throw new BusinessException(HttpStatus.CONFLICT,"User " + userId + " already has a wallet");
        }

        UserInfo user = userClient.getUser(userId);

        if (!"ACTIVE".equals(user.status())){
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY,"User is not Active");
        }

        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("IDR");
        wallet = walletRepository.save(wallet);

        return new BalanceResponse(wallet.getId(),userId,wallet.getBalance(), wallet.getCurrency());
    }
}
