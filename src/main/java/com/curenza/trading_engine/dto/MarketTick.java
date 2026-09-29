package com.curenza.trading_engine.dto;

import java.math.BigDecimal;

public record MarketTick(
        String symbol,
        BigDecimal currentPrice,
        long timestamp
) {
}
