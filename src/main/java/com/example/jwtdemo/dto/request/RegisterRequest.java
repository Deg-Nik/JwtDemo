package com.example.jwtdemo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username обязателен")
    @Size(min = 3, max = 50, message = "Username от 3 до 50 символов")
    @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Username может содержать только буквы, цифры и _"
    )
    private String username;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Минимальная длина пароля — 6 символов")
    private String password;

    @NotBlank
    @Email
    private String email;

}
