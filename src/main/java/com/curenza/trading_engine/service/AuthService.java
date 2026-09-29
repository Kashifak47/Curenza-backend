package com.curenza.trading_engine.service;

import com.curenza.trading_engine.dto.AuthResponse;
import com.curenza.trading_engine.dto.LoginRequest;
import com.curenza.trading_engine.dto.RegisterRequest;
import com.curenza.trading_engine.entity.User;
import com.curenza.trading_engine.repository.UserRepository;
import com.curenza.trading_engine.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private  final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request){
        if (userRepository.existsByEmail(request.email())){
            throw new IllegalArgumentException("Email already in use");
        }
        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email().toLowerCase().trim())
                .password(passwordEncoder.encode(request.password()))
                .demoBalance(BigDecimal.valueOf(10000.00))
                .realBalance(BigDecimal.ZERO)
                .build();
        User savedUser = userRepository.save(user);
        String token = jwtUtil.generateToken(savedUser.getEmail());

        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getDemoBalance(),
                savedUser.getRealBalance()
        );
    }

     public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.email().toLowerCase().trim())
                .orElseThrow(()-> new IllegalArgumentException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPassword())){
            throw new IllegalArgumentException("Invalid email or password");
        }
        String token = jwtUtil.generateToken(user.getEmail());
        return  new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getDemoBalance(),
                user.getRealBalance()
        );
     }
}
