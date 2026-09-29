package com.curenza.trading_engine.service;

import com.curenza.trading_engine.dto.TradeHistoryResponse;
import com.curenza.trading_engine.dto.TradeRequest;
import com.curenza.trading_engine.dto.TradeResponse;
import com.curenza.trading_engine.entity.Trade;
import com.curenza.trading_engine.entity.TradeStatus;
import com.curenza.trading_engine.entity.User;
import com.curenza.trading_engine.repository.TradeRepository;
import com.curenza.trading_engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;

    // Standard platform payout rate (82%)
    private static final double PAYOUT_RATE = 0.82;

    // @Transactional ensures that if any part of this fails, EVERYTHING rolls back.
    // No money is lost in the void!
    @Transactional
    public TradeResponse placeTrade(String email, TradeRequest request){
        // 1. Find the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        // 2. Validate balance
        if(user.getDemoBalance().compareTo(request.amount())<0){
            throw new RuntimeException("Insufficient funds. Balance: $" + user.getDemoBalance());
        }

        // 3. Deduct the stake from the user's balance immediately
        user.setDemoBalance(user.getDemoBalance().subtract(request.amount()));
        userRepository.save(user);

        // 4. Lock in the EXACT market price at this millisecond
        BigDecimal currentPrice = marketDataService.getCurrentPrice(request.symbol());
        if (currentPrice.compareTo(BigDecimal.ZERO) == 0){
            throw new RuntimeException("Market data temporarily unavailable");
        }

        // 5. Calculate timings
        long now = System.currentTimeMillis();
        long expiryTime = now + (request.durationSeconds() * 1000L);

        // 6. Build and save the Trade ticket to PostgreSQL
        Trade trade = Trade.builder()
                .user(user)
                .symbol(request.symbol())
                .direction(request.direction())
                .amount(request.amount())
                .entryPrice(currentPrice)
                .status(TradeStatus.OPEN)
                .openTime(now)
                .expiryTime(expiryTime)
                .payoutRate(PAYOUT_RATE)
                .build();
        trade = tradeRepository.save(trade);

        // 7. Return the successful trade data to React
        return new TradeResponse(
                trade.getId(),
                trade.getSymbol(),
                trade.getDirection().name(),
                trade.getAmount(),
                trade.getEntryPrice(),
                trade.getOpenTime(),
                trade.getExpiryTime()
        );
    }

    @Transactional(readOnly = true)
    public List<TradeHistoryResponse> getTradeHistory(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        return tradeRepository.findByUserOrderByOpenTimeDesc(user)
                .stream()
                .map(trade -> new TradeHistoryResponse(
                        trade.getId(),
                        trade.getSymbol(),
                        trade.getDirection().name(),
                        trade.getAmount(),
                        trade.getEntryPrice(),
                        trade.getClosePrice(),
                        trade.getProfitAmount(),
                        trade.getStatus().name(),
                        trade.getOpenTime(),
                        trade.getExpiryTime(),
                        trade.getPayoutRate()
                ))
                .collect(Collectors.toList());
    }
}
