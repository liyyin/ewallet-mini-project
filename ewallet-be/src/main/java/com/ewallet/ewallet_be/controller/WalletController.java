package com.ewallet.ewallet_be.controller;

import com.ewallet.ewallet_be.dto.*;
import com.ewallet.ewallet_be.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/{id}/balance")
    public ApiResponse<BalanceResponse> getBalance(@PathVariable Long id){
        BalanceResponse result = walletService.getBalance(id);
        return ApiResponse.ok(result);
    }

    @PostMapping("/{id}/topup")
    public ApiResponse<TransactionResponse> topUp(@PathVariable Long id,
                                                  @Valid @RequestBody TopUpRequest request){
        TransactionResponse transactionResponse = walletService.topUp(id,request);
        return ApiResponse.ok(transactionResponse);
    }

    @PostMapping("/transfer")
    public ApiResponse<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request){
        TransactionResponse transactionResponse = walletService.transfer(request);
        return ApiResponse.ok(transactionResponse);
    }

    @GetMapping("/{id}/mutations")
    public ApiResponse<List<MutationResponse>> getMutations(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "20") int limit){
        List<MutationResponse> result = walletService.getMutations(id, from, to, limit);
        return ApiResponse.ok(result);
    }

    @GetMapping("/{id}/mutations/summary")
    public ApiResponse<WalletSummaryResponse> getSummary(@PathVariable Long id){
        WalletSummaryResponse result = walletService.getSummary(id);
        return ApiResponse.ok(result);
    }

    @PostMapping
    public ApiResponse<BalanceResponse> createWallet(@Valid @RequestBody CreateWalletRequest request){
        BalanceResponse response = walletService.createWallet(request);
        return ApiResponse.ok(response);
    }
}
