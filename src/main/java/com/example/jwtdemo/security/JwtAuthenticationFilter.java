package com.example.jwtdemo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * ===============================================
 * JWT AUTHENTICATION FILTER
 * ===============================================
 *
 * Выполняется на КАЖДОМ запросе!
 *
 * Задачи:
 * 1. Извлечь JWT токен из заголовка Authorization
 * 2. Валидировать токен
 * 3. Извлечь username из токена
 * 4. Загрузить UserDetails из БД
 * 5. Установить Authentication в SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // ШАГ 1: Извлечь токен из заголовка
            String jwt = getJwtFromRequest(request);

            // ШАГ 2: Если токен есть и он валидный
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // ШАГ 3: Извлечь username из токена
                String username = tokenProvider.getUsernameFromToken(jwt);

                // ШАГ 4: Загрузить UserDetails из БД
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // ШАГ 5: Создать Authentication объект
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // ШАГ 6: Установить Authentication в SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
        }

        // ШАГ 7: Продолжить цепочку фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Извлечение JWT из заголовка Authorization
     *
     * Формат: Authorization: Bearer <token>
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Убираем "Bearer "
        }

        return null;
    }
}
