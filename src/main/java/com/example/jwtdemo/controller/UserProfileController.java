package com.example.jwtdemo.controller;

import com.example.jwtdemo.dto.request.UserProfileUpdateRequest;
import com.example.jwtdemo.dto.response.UserProfileResponse;
import com.example.jwtdemo.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author : Nikolai Degtiarev
 * created : 23.03.26
 *
 *
 **/
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userService;

    @GetMapping
    public UserProfileResponse getMe() {
        return userService.getProfile();
    }

    @PutMapping
    public UserProfileResponse updateMe(@Valid @RequestBody UserProfileUpdateRequest request) {
        return userService.updateProfile(request);
    }
}

