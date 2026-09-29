package com.curenza.trading_engine.dto;

import com.curenza.trading_engine.entity.TradeDirection;

import java.math.BigDecimal;

public record TradeRequest(
        String symbol,
        TradeDirection direction,  // "UP" or "DOWN"
        BigDecimal amount,
        int durationSeconds
) {
}
