package com.curenza.trading_engine.controller;

import com.curenza.trading_engine.dto.TradeHistoryResponse;
import com.curenza.trading_engine.dto.TradeRequest;
import com.curenza.trading_engine.dto.TradeResponse;
import com.curenza.trading_engine.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    // 1. PLACE A NEW TRADE
    @PostMapping
    public ResponseEntity<TradeResponse> placeOrder(Authentication authentication, @RequestBody TradeRequest request){
        String email = authentication.getName();
        TradeResponse response = tradeService.placeTrade(email, request);
        return ResponseEntity.ok(response);
    }

    // 2. GET USER'S TRADE HISTORY
    @GetMapping
    public ResponseEntity<List<TradeHistoryResponse>> getTradeHistory(Authentication authentication){
        String email = authentication.getName();
        List<TradeHistoryResponse> history = tradeService.getTradeHistory(email);
        return ResponseEntity.ok(history);
    }
}
