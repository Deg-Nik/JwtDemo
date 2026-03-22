package com.example.jwtdemo.controller;

import com.example.jwtdemo.dto.request.NoteRequest;
import com.example.jwtdemo.dto.response.NoteResponse;
import com.example.jwtdemo.entity.Note;
import com.example.jwtdemo.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    /**
     * Получить все мои заметки
     */
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getMyNotes() {
        List<Note> notes = noteService.getMyNotes();

        List<NoteResponse> response = notes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Получить заметку по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNoteById(@PathVariable Long id) {
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(toResponse(note));
    }

    /**
     * Создать заметку
     */
    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@Valid @RequestBody NoteRequest request) {
        Note note = noteService.createNote(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toResponse(note));
    }

    /**
     * Обновить заметку
     */
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody NoteRequest request) {

        Note note = noteService.updateNote(id, request);
        return ResponseEntity.ok(toResponse(note));
    }

    /**
     * Удалить заметку
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) {
        noteService.deleteNote(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Mapper Note -> NoteResponse
     */
    private NoteResponse toResponse(Note note) {
        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .username(note.getUser().getUsername())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}