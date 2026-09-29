package com.curenza.trading_engine.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // "user" is a reserved keyword in PostgreSQL, so we use "users"
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String fullName;

    // RULE: Never use Double or Float for money. Always use BigDecimal.
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal realBalance;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal demoBalance;

    @Column(nullable = false)
    private String role;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    // This runs automatically right before a new user is saved to the database
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();

        // Every new user automatically gets $10,000 in demo funds
        if (this.demoBalance == null) {
            this.demoBalance = new BigDecimal("10000.00");
        }
        if (this.realBalance == null) {
            this.realBalance = BigDecimal.ZERO;
        }
        if (this.role == null) {
            this.role = "ROLE_USER";
        }
    }
}
