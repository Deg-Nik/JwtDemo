package com.example.jwtdemo.service;

import com.example.jwtdemo.dto.request.NoteRequest;
import com.example.jwtdemo.entity.Note;
import com.example.jwtdemo.entity.User;
import com.example.jwtdemo.repository.NoteRepository;
import com.example.jwtdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    /**
     * Получить все заметки текущего пользователя
     */
    public List<Note> getMyNotes() {
        User currentUser = getCurrentUser();
        return noteRepository.findByUser(currentUser);
    }

    /**
     * Получить заметку по ID (только свою!)
     */
    public Note getNoteById(Long id) {
        User currentUser = getCurrentUser();
        return noteRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Note not found or access denied"));
    }

    /**
     * Создать заметку
     */
    @Transactional
    public Note createNote(NoteRequest request) {
        User currentUser = getCurrentUser();

        Note note = new Note();
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setUser(currentUser);

        return noteRepository.save(note);
    }

    /**
     * Обновить заметку
     */
    @Transactional
    public Note updateNote(Long id, NoteRequest request) {
        Note note = getNoteById(id); // Проверка что заметка принадлежит пользователю

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        return noteRepository.save(note);
    }

    /**
     * Удалить заметку
     */
    @Transactional
    public void deleteNote(Long id) {
        Note note = getNoteById(id); // Проверка что заметка принадлежит пользователю
        noteRepository.delete(note);
    }

    /**
     * Получить текущего пользователя из SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}