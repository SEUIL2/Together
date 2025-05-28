package com.together.user.professor.projectmemo;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.professor.projectmemo.dto.ProfessorProjectMemoDto;
import com.together.user.UserRepository;
import com.together.user.professor.ProfessorEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfessorProjectMemoService {

    private final ProfessorProjectMemoRepository memoRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // ✅ 메모 작성
    @Transactional
    public ProfessorProjectMemoDto createMemo(Long professorId, Long projectId, String content) {
        ProfessorEntity professor = (ProfessorEntity) userRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("교수 정보를 찾을 수 없습니다."));

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트 정보를 찾을 수 없습니다."));

        ProfessorProjectMemoEntity memo = ProfessorProjectMemoEntity.builder()
                .professor(professor)
                .project(project)
                .content(content)
                .build();

        ProfessorProjectMemoEntity saved = memoRepository.save(memo);

        return toDto(saved);
    }

    // ✅ 프로젝트별 메모 목록 조회
    public List<ProfessorProjectMemoDto> getMemosByProject(Long projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        return memoRepository.findByProject(project).stream()
                .map(this::toDto)
                .toList();
    }

    // ✅ 메모 수정
    @Transactional
    public ProfessorProjectMemoDto updateMemo(Long memoId, String newContent) {
        ProfessorProjectMemoEntity memo = memoRepository.findById(memoId)
                .orElseThrow(() -> new RuntimeException("메모를 찾을 수 없습니다."));

        memo.setContent(newContent); // 내용 수정
        memo.setUpdatedAt(new Date());

        return toDto(memo);
    }

    // ✅ 메모 삭제
    @Transactional
    public void deleteMemo(Long memoId) {
        memoRepository.deleteById(memoId);
    }

    // ✅ Entity → DTO 변환 메서드
    private ProfessorProjectMemoDto toDto(ProfessorProjectMemoEntity entity) {
        return new ProfessorProjectMemoDto(
                entity.getId(),
                entity.getProject().getProjectId(),
                entity.getContent(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
