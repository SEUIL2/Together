package com.together.privateNote;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import com.together.user.student.StudentEntity;
import com.together.user.student.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PrivateNoteService {

    private final PrivateNoteRepository noteRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProjectRepository projectRepository;

    //메모 생성
    public PrivateNoteEntity createNote(Long authorId, Long projectId, PrivateNoteDto dto) {
        UserEntity author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("작성자 없음"));

        StudentEntity target = studentRepository.findById(dto.getTargetStudentId())
                .orElseThrow(() -> new RuntimeException("대상 학생 없음"));

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        // 자기 자신에게 쓰는 건 금지
        if (author.getUserId().equals(target.getUserId())) {
            throw new IllegalArgumentException("자기 자신에게 메모를 작성할 수 없습니다.");
        }

        PrivateNoteEntity note = new PrivateNoteEntity();
        note.setAuthor(author);
        note.setTargetStudent(target);
        note.setContent(dto.getContent());
        note.setProject(project);

        return noteRepository.save(note);
    }

    //특정 메모 조회
    public Optional<PrivateNoteEntity> getNotesByAuthorAndTarget(Long authorId, Long targetStudentId) {
        return noteRepository.findByAuthorUserIdAndTargetStudentUserId(authorId, targetStudentId);
    }

    @Transactional
    public PrivateNoteEntity updateNote(Long authorId, Long studentId, String content) {
        PrivateNoteEntity note = noteRepository.findByAuthorUserIdAndTargetStudentUserId(authorId, studentId)
                .orElseThrow(() -> new RuntimeException("작성한 메모가 없습니다."));

        note.setContent(content);
        return noteRepository.save(note);
    }

    //메모 중복생성 방지
    public boolean existsNote(Long authorId, Long studentId) {
        return noteRepository.findByAuthorUserIdAndTargetStudentUserId(authorId, studentId).isPresent();
    }

    public PrivateNoteResponseDto toResponse(PrivateNoteEntity note) {
        return new PrivateNoteResponseDto(
                note.getNoteId(),
                note.getTargetStudent().getUserId(),
                note.getTargetStudent().getUserName(),
                note.getAuthor().getUserId(),
                note.getAuthor().getUserName(),
                note.getContent(),
                note.getCreatedAt()
        );
    }
}
