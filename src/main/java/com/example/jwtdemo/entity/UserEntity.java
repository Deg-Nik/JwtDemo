package com.example.jwtdemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users") // Хорошая практика - явно указывать имя таблицы во множественном числе
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    // В базе данных мы храним именно хеш, а не сам пароль.
    // Длина колонки обычно делается с запасом (например, BCrypt генерирует строку в 60 символов)
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    // Простая реализация ролей.
    // В реальных проектах это часто выносят в отдельную сущность RoleEntity и связывают через @ManyToMany
    @Column(nullable = false, length = 50)
    private String role;
}