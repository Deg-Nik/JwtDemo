package com.example.jwtdemo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ===============================================
 * JWT TOKEN PROVIDER - ЦЕНТРАЛЬНЫЙ КОМПОНЕНТ
 * ===============================================
 *
 * Отвечает за:
 * 1. Генерацию Access Token
 * 2. Генерацию Refresh Token
 * 3. Валидацию токенов
 * 4. Извлечение данных из токенов
 */
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration; // 15 минут

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration; // 7 дней

    /**
     * ===============================================
     * 1. ГЕНЕРАЦИЯ ACCESS TOKEN
     * ===============================================
     */
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());

        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    /**
     * ===============================================
     * 2. ГЕНЕРАЦИЯ REFRESH TOKEN
     * ===============================================
     */
    public String generateRefreshToken(UserDetails userDetails) {
        return createToken(new HashMap<>(), userDetails.getUsername(), refreshTokenExpiration);
    }

    /**
     * ===============================================
     * ВСПОМОГАТЕЛЬНЫЙ МЕТОД - СОЗДАНИЕ ТОКЕНА
     * ===============================================
     */
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)              // Кастомные данные
                .setSubject(subject)            // Username
                .setIssuedAt(now)               // Время создания
                .setExpiration(expiryDate)      // Время истечения
                .signWith(getSigningKey())      // Подпись
                .compact();
    }

    /**
     * ===============================================
     * 3. ИЗВЛЕЧЕНИЕ USERNAME ИЗ ТОКЕНА
     * ===============================================
     */
    public String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * ===============================================
     * 4. ВАЛИДАЦИЯ ТОКЕНА
     * ===============================================
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (SecurityException ex) {
            System.err.println("Invalid JWT signature");
        } catch (MalformedJwtException ex) {
            System.err.println("Invalid JWT token");
        } catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token");
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token");
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty");
        }
        return false;
    }

    /**
     * ===============================================
     * ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
     * ===============================================
     */

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * ===============================================
     * 5. ПОЛУЧИТЬ EXPIRATION TIME
     * ===============================================
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }
}