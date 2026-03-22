package com.example.jwtdemo.service;

import com.example.jwtdemo.dto.request.LoginRequest;
import com.example.jwtdemo.dto.request.RefreshTokenRequest;
import com.example.jwtdemo.dto.request.RegisterRequest;
import com.example.jwtdemo.dto.response.JwtResponse;
import com.example.jwtdemo.entity.User;
import com.example.jwtdemo.entity.RefreshToken;
import com.example.jwtdemo.repository.RefreshTokenRepository;
import com.example.jwtdemo.repository.UserRepository;
import com.example.jwtdemo.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    /**
     * ===============================================
     * РЕГИСТРАЦИЯ
     * ===============================================
     */
    @Transactional
    public User register(RegisterRequest request) {
        // Проверка уникальности username
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username уже занят");
        }

        // Проверка уникальности email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email уже используется");
        }

        // Создание пользователя
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        user.setEnabled(true);

        return userRepository.save(user);
    }

    /**
     * ===============================================
     * LOGIN (ВХОД)
     * ===============================================
     */
    @Transactional
    public JwtResponse login(LoginRequest request) {
        // ШАГ 1: Аутентификация через Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // ШАГ 2: Установка аутентификации в контекст
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ШАГ 3: Загрузка UserDetails
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // ШАГ 4: Генерация Access Token
        String accessToken = tokenProvider.generateAccessToken(userDetails);

        // ШАГ 5: Генерация Refresh Token
        String refreshToken = tokenProvider.generateRefreshToken(userDetails);

        // ШАГ 6: Сохранение Refresh Token в БД
        saveRefreshToken(request.getUsername(), refreshToken);

        // ШАГ 7: Возврат токенов
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .username(userDetails.getUsername())
                .build();
    }

    /**
     * ===============================================
     * REFRESH (ОБНОВЛЕНИЕ ТОКЕНОВ)
     * ===============================================
     */
    @Transactional
    public JwtResponse refresh(RefreshTokenRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        // ШАГ 1: Валидация refresh token
        if (!tokenProvider.validateToken(refreshTokenValue)) {
            throw new RuntimeException("Invalid refresh token");
        }

        // ШАГ 2: Проверка что токен есть в БД и не revoked
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getRevoked()) {
            throw new RuntimeException("Refresh token был отозван");
        }

        if (refreshToken.isExpired()) {
            throw new RuntimeException("Refresh token истёк");
        }

        // ШАГ 3: Загрузка пользователя
        String username = tokenProvider.getUsernameFromToken(refreshTokenValue);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // ШАГ 4: Генерация НОВЫХ токенов
        String newAccessToken = tokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = tokenProvider.generateRefreshToken(userDetails);

        // ШАГ 5: Отзыв старого refresh token (Refresh Token Rotation!)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        // ШАГ 6: Сохранение нового refresh token
        saveRefreshToken(username, newRefreshToken);

        // ШАГ 7: Возврат новых токенов
        return JwtResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenExpiration())
                .username(userDetails.getUsername())
                .build();
    }

    /**
     * ===============================================
     * LOGOUT (ВЫХОД)
     * ===============================================
     */
    @Transactional
    public void logout() {
        // Получаем текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Находим пользователя
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Удаляем все refresh токены пользователя
        refreshTokenRepository.deleteByUser(user);
    }

    /**
     * ===============================================
     * ВСПОМОГАТЕЛЬНЫЙ МЕТОД - СОХРАНЕНИЕ REFRESH TOKEN
     * ===============================================
     */
    private void saveRefreshToken(String username, String tokenValue) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(tokenValue);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(7)); // 7 дней
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }
}