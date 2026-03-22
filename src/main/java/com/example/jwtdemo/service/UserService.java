package com.example.jwtdemo.service;

import com.example.jwtdemo.entity.User;
import com.example.jwtdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь с именем " + username + " не найден."));
    }

//    public User updateRole(String username, String role) {
//        User currentUser = findByUsername(username);
//
//        return userRepository.setRole(role)
//                .orElseThrow(() -> new RuntimeException("Для пользователя не возможно обновить роль"));
//
//    }
}
