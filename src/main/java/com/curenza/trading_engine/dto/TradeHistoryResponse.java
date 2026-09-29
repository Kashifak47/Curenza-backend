package com.curenza.trading_engine.dto;

import java.math.BigDecimal;

public record TradeHistoryResponse(
        Long id,
        String symbol,
        String direction,
        BigDecimal amount,
        BigDecimal entryPrice,
        BigDecimal closePrice,
        BigDecimal profitAmount,
        String status,
        Long openTime,
        Long expiryTime,
        Double payoutRate
) {
}
