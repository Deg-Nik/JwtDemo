package com.example.jwtdemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : Nikolai Degtiarev
 * created : 23.03.26
 *
 *
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    private Long id;

    private String username;

    private String email;

    private String role;
}

