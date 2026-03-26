package com.example.jwtdemo.repository;

import com.example.jwtdemo.entity.RefreshToken;
import com.example.jwtdemo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByUser(UserEntity user);
}
