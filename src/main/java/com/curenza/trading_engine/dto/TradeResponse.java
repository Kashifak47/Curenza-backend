package com.curenza.trading_engine.dto;

import java.math.BigDecimal;

public record TradeResponse(
        Long id,
        String symbol,
        String direction,
        BigDecimal amount,
        BigDecimal entryPrice,
        Long openTime,
        Long expiryTime
) {
}
