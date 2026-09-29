package com.curenza.trading_engine.service;

import com.curenza.trading_engine.dto.MarketTick;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class MarketDataService {

    private final SimpMessagingTemplate messagingTemplate;

    // EXPANDED: 9 Major and Minor Currency Pairs
    private final Map<String, BigDecimal> currentPrices = new ConcurrentHashMap<>(Map.of(
            "EUR/USD", new BigDecimal("1.09452"),
            "GBP/USD", new BigDecimal("1.26310"),
            "USD/JPY", new BigDecimal("150.250"),
            "AUD/USD", new BigDecimal("0.65420"),
            "USD/CAD", new BigDecimal("1.35210"),
            "USD/CHF", new BigDecimal("0.88140"),
            "NZD/USD", new BigDecimal("0.61230"),
            "EUR/GBP", new BigDecimal("0.85410"),
            "EUR/JPY", new BigDecimal("164.450")
    ));

    /*
     * WHEN YOU SWITCH TO REAL DATA:
     * Simply delete this @Scheduled method, and replace it with a WebSocket listener
     * or Webhook from your data provider that updates 'currentPrices' and calls
     * messagingTemplate.convertAndSend() with the real MarketTick.
     */
    @Scheduled(fixedRate = 1000)
    public void simulateAndBroadcastMarketData() {
        currentPrices.forEach((symbol, oldPrice) -> {

            // JPY pairs require different volatility scaling and 3 decimal places instead of 5
            boolean isJpy = symbol.endsWith("JPY");
            double volatilityBase = isJpy ? 0.050 : 0.00025;
            int scale = isJpy ? 3 : 5;

            // Calculate random movement up or down
            double volatility = (ThreadLocalRandom.current().nextDouble() - 0.5) * volatilityBase;

            BigDecimal newPrice = oldPrice.add(BigDecimal.valueOf(volatility))
                    .setScale(scale, RoundingMode.HALF_UP);

            // Update memory and broadcast
            currentPrices.put(symbol, newPrice);
            MarketTick tick = new MarketTick(symbol, newPrice, System.currentTimeMillis());

            messagingTemplate.convertAndSend("/topic/prices", tick);
        });
    }

    // Used later by the OrderService to lock in the exact price when a trade is clicked
    public BigDecimal getCurrentPrice(String symbol) {
        return currentPrices.getOrDefault(symbol, BigDecimal.ZERO);
    }
}