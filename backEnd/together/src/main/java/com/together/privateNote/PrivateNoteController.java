package com.together.privateNote;

import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
public class PrivateNoteController {

    private final PrivateNoteService noteService;

    /**
     * @param userDetails
     * @param projectId
     * @param dto
     * 예시 :
     * {
     *    "targetStudentId" : "1",
     *    "content" : "메모 테스트"
     * }
     */
    @PostMapping("/create")
    public ResponseEntity<?> createNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @CurrentProject Long projectId,
            @RequestBody PrivateNoteDto dto
    ) {
        Long authorId = userDetails.getUser().getUserId();
        // 이미 메모 존재하면 409 Conflict
        if (noteService.existsNote(authorId, dto.getTargetStudentId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 메모가 존재합니다."));
        }

        PrivateNoteEntity createNote = noteService.createNote(userDetails.getUser().getUserId(), projectId, dto);

        PrivateNoteResponseDto response = new PrivateNoteResponseDto(
                createNote.getNoteId(),
                createNote.getTargetStudent().getUserId(),
                createNote.getTargetStudent().getUserName(),
                createNote.getAuthor().getUserId(),
                createNote.getAuthor().getUserName(),
                createNote.getContent(),
                createNote.getCreatedAt()
        );
        return ResponseEntity.ok(response);
    }

    //특정 학생 메모 조회
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<PrivateNoteResponseDto>> getNotesByTargetStudent(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long studentId
    ) {
        Long currentUserId = userDetails.getUser().getUserId();

        Optional<PrivateNoteEntity> notes = noteService.getNotesByAuthorAndTarget(currentUserId, studentId);

        List<PrivateNoteResponseDto> responseList = notes.stream()
                .map(note -> new PrivateNoteResponseDto(
                        note.getNoteId(),
                        note.getTargetStudent().getUserId(),
                        note.getTargetStudent().getUserName(),
                        note.getAuthor().getUserId(),
                        note.getAuthor().getUserName(),
                        note.getContent(),
                        note.getCreatedAt()
                ))
                .toList();

        return ResponseEntity.ok(responseList);
    }

    /**
     * 메모 수정
     * @param userDetails
     * @param studentId
     * @param body
     * 예시
     * {
     *     "content" : "수정 테스트"
     * }
     * @return
     */
    @PutMapping("/update/{studentId}")
    public ResponseEntity<?> updateNote(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long studentId,
            @RequestBody Map<String, String> body
    ) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("내용이 비어 있습니다.");
        }

        PrivateNoteEntity updated = noteService.updateNote(userDetails.getUser().getUserId(), studentId, content);
        return ResponseEntity.ok(noteService.toResponse(updated));
    }

}
