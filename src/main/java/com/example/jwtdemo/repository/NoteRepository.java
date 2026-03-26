package com.example.jwtdemo.repository;

import com.example.jwtdemo.entity.Note;
import com.example.jwtdemo.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUser(UserEntity user);

    Optional<Note> findByIdAndUser(Long id, UserEntity user);
}
