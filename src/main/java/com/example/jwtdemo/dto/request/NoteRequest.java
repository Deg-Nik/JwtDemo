package com.example.jwtdemo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoteRequest {
    @NotBlank
    @Size(min = 1, max = 200)
    private String title;

    @Size(max = 5000)
    private String content;
}
