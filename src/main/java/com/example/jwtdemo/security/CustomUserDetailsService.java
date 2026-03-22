package com.example.jwtdemo.security;

import com.example.jwtdemo.entity.UserEntity;
import com.example.jwtdemo.repository.UserEntityRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService, UserDetailsPasswordService {

    private final UserEntityRepository userRepository; // Ваш репозиторий для работы с БД

    public CustomUserDetailsService(UserEntityRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 1. Метод для первоначальной загрузки пользователя при попытке входа
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));

        // Преобразуем сущность БД в объект, понятный Spring Security
        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPasswordHash()) // Здесь лежит старый хеш
                .roles(userEntity.getRole())
                .build();
    }

    // 2. Метод для автоматического обновления хеша пароля
    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails user, String newPasswordHash) {
        // Spring Security сам вычислил новый хеш (newPasswordHash) и передал его сюда.
        // Наша задача — просто сохранить его в БД.

        UserEntity userEntity = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        userEntity.setPasswordHash(newPasswordHash);
        userRepository.save(userEntity);

        // Возвращаем обновленного пользователя в контекст Security
        return User.builder()
                .username(user.getUsername())
                .password(newPasswordHash)
                .roles(userEntity.getRole())
                .build();
    }
}