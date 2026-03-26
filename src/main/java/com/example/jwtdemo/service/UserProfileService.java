package com.example.jwtdemo.service;

import com.example.jwtdemo.dto.request.UserProfileUpdateRequest;
import com.example.jwtdemo.dto.response.UserProfileResponse;
import com.example.jwtdemo.entity.UserEntity;
import com.example.jwtdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * @author : Nikolai Degtiarev
 * created : 23.03.26
 *
 *
 **/
@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;

    public UserEntity getCurrentUser() {
        return (UserEntity) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public UserProfileResponse getProfile() {
        UserEntity user = getCurrentUser();

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }

    public UserProfileResponse updateProfile(UserProfileUpdateRequest request) {
        UserEntity user = getCurrentUser();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        userRepository.save(user);

        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        );
    }
}
