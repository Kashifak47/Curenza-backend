package com.curenza.trading_engine.repository;

import com.curenza.trading_engine.entity.Trade;
import com.curenza.trading_engine.entity.TradeStatus;
import com.curenza.trading_engine.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TradeRepository extends JpaRepository<Trade, Long> {
    // Grabs a user's entire trade history, newest first
    List<Trade> findByUserOrderByOpenTimeDesc(User user);

    // Used by the background engine to find trades that need to be resolved
    List<Trade> findByStatus(TradeStatus status);

    // Finds trades that are OPEN and have passed their expiration time
    List<Trade> findByStatusAndExpiryTimeLessThanEqual(TradeStatus status, Long currentTime);
}
