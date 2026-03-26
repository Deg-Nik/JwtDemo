package com.example.jwtdemo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author : Nikolai Degtiarev
 * created : 23.03.26
 *
 *
 **/
@Data
public class UserProfileUpdateRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;
}
