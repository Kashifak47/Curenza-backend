package com.curenza.trading_engine.dto;

import java.math.BigDecimal;

public record AuthResponse(
       String token,
       String email,
       String fullName,
       BigDecimal demoBalance,
       BigDecimal realBalance
) {}
