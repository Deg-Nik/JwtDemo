package com.example.jwtdemo.config;

import com.example.jwtdemo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter; // Ваш JWT фильтр из предыдущего шага

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                // Отключаем CSRF, так как при JWT (stateless) он обычно не нужен
                .csrf(AbstractHttpConfigurer::disable)

                // Настраиваем правила доступа к эндпоинтам
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Публичные эндпоинты (логин, регистрация)
                        .anyRequest().authenticated()                // Все остальные требуют токен
                )

                // Указываем, что сессии не создаются (Stateless архитектура)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Подключаем наш кастомный провайдер
                .authenticationProvider(authenticationProvider)

                // Вставляем JWT фильтр ПЕРЕД стандартным фильтром проверки логина/пароля
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        // 1. Инициализируем через новый обязательный конструктор
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);

        // 2. Устанавливаем энкодер для сверки паролей (сеттер для него пока оставили)
        provider.setPasswordEncoder(passwordEncoder);

        // 3. Передаем сервис обновления хешей (с обязательным аргументом)
        if (userDetailsService instanceof UserDetailsPasswordService passwordService) {
            provider.setUserDetailsPasswordService(passwordService);
        }

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Используем современный алгоритм.
        // Если до этого пароли хранились иначе, механизм UserDetailsPasswordService их обновит при логине.
        return new BCryptPasswordEncoder();
    }
}