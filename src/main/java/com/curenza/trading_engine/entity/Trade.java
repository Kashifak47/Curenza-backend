package com.curenza.trading_engine.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Links this trade securely to the user who placed it
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String symbol;   // e.g., EUR/USD
    @Enumerated(EnumType.STRING)
    private TradeDirection direction;  // UP or DOWN
    private BigDecimal amount; // The stake size

    @Column(precision = 10, scale = 5)
    private BigDecimal entryPrice;

    @Column(precision = 10, scale = 5)
    private BigDecimal closePrice;

    private BigDecimal profitAmount;  // Negative if lost, positive if won
    @Enumerated(EnumType.STRING)
    private TradeStatus status;   // OPEN, WIN, LOSS, TIE
    private Long openTime;   //Milliseconds timestamp
    private Long expiryTime;   //Milliseconds timestamp
    private Double payoutRate;  // e.g., 0.82 (82%)
}
