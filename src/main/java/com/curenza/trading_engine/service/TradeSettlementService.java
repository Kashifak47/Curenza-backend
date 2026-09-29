package com.curenza.trading_engine.service;

import com.curenza.trading_engine.entity.Trade;
import com.curenza.trading_engine.entity.TradeDirection;
import com.curenza.trading_engine.entity.TradeStatus;
import com.curenza.trading_engine.entity.User;
import com.curenza.trading_engine.repository.TradeRepository;
import com.curenza.trading_engine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSettlementService {

    private final TradeRepository tradeRepository;
    private final UserRepository userRepository;
    private final MarketDataService marketDataService;

    // Runs every 1000ms (1 second) to sweep for expired trades
    @Scheduled(fixedRate = 1000)
    @Transactional
    public void settleExpiredTrades(){
        long now = System.currentTimeMillis();

        // Grab all trades that are OPEN but their time is up
        List<Trade> expiredTrades = tradeRepository.findByStatusAndExpiryTimeLessThanEqual(TradeStatus.OPEN, now);

        for (Trade trade : expiredTrades){
            BigDecimal closePrice = marketDataService.getCurrentPrice(trade.getSymbol());
            trade.setClosePrice(closePrice);

            boolean isWin = false;
            boolean isTie = closePrice.compareTo(trade.getEntryPrice())==0;

            // Determine if the prediction was correct
            if(!isTie){
                if(trade.getDirection() == TradeDirection.UP && closePrice.compareTo(trade.getEntryPrice()) > 0){
                    isWin = true;
                }else if(trade.getDirection() == TradeDirection.DOWN && closePrice.compareTo(trade.getEntryPrice())< 0){
                    isWin = true;
                }
            }

            User user = trade.getUser();
            BigDecimal payout = BigDecimal.ZERO;

            if (isWin){
                trade.setStatus(TradeStatus.WIN);
                // Profit = Stake * Payout Rate
                BigDecimal profit = trade.getAmount().multiply(BigDecimal.valueOf(trade.getPayoutRate()));
                trade.setProfitAmount(profit);

                // Return original stake + profit
                payout = trade.getAmount().add(profit);
            } else if (isTie) {
                trade.setStatus(TradeStatus.TIE);
                trade.setProfitAmount(BigDecimal.ZERO);
                // Return just the original stake
                payout = trade.getAmount();
            }else{
                trade.setStatus(TradeStatus.LOSS);
                // Profit is negative the stake
                trade.setProfitAmount(trade.getAmount().negate());
                // Payout is 0 (money was already deducted when trade was placed)
            }

            // Update user balance if they won or tied
            if(payout.compareTo(BigDecimal.ZERO) > 0){
                user.setDemoBalance(user.getDemoBalance().add(payout));
                userRepository.save(user);
            }

            // Save the finalized trade ticket
            tradeRepository.save(trade);

            log.info("Setteled Trade #{}: {} {} - Result: {}", trade.getId(), trade.getDirection(),trade.getSymbol(),trade.getStatus());


        }

    }
}
