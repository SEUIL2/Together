package com.together.comment;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    /**
     * 🔎 댓글 트리 조회
     * - 특정 위치(targetId)에 달린 모든 댓글을 시간순으로 가져와서
     * - 부모-자식 관계로 연결된 "트리 구조"로 반환
     */
    public List<CommentDto> getCommentTree(Long targetId, CommentEntity.CommentType type) {
        List<CommentEntity> allComments = commentRepository.findByTargetIdAndCommentTypeOrderByCreatedAtAsc(targetId, type);

        Map<Long, CommentDto> dtoMap = new HashMap<>(); // ID 기준으로 댓글 저장
        List<CommentDto> rootList = new ArrayList<>(); // 최상위 댓글 리스트

        for (CommentEntity entity : allComments) {
            CommentDto dto = CommentDto.builder()
                    .id(entity.getId())
                    .content(entity.isDeleted() ? "[삭제된 댓글입니다]" : entity.getContent())
                    .authorName(entity.getUser().getUserName())
                    .isDeleted(entity.isDeleted())
                    .createdAt(entity.getCreatedAt())
                    .children(new ArrayList<>())
                    .build();

            dtoMap.put(entity.getId(), dto);

            if (entity.getParent() == null) {
                rootList.add(dto); // 부모가 없으면 루트 댓글
            } else {
                // 부모 댓글이 있다면 자식으로 연결
                CommentDto parentDto = dtoMap.get(entity.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto); // 자식 댓글 연결
                }
            }
        }

        return rootList;
    }

    /**
     * 📝 댓글 작성
     * - 사용자, 프로젝트, 부모댓글을 확인하고
     * - 새로운 댓글을 생성해서 DB에 저장
     */
    @Transactional
    public CommentEntity createComment(String content, Long userId, Long projectId, Long targetId,
                                       CommentEntity.CommentType type, Long parentId) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다"));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다"));

        CommentEntity parent = null;
        if (parentId != null) {
            parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("부모 댓글이 존재하지 않습니다"));
        }

        CommentEntity comment = CommentEntity.builder()
                .content(content)
                .user(user)
                .project(project)
                .parent(parent)
                .targetId(targetId)
                .commentType(type)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isDeleted(false)
                .build();

        return commentRepository.save(comment);
    }

    /**
     * 🗑️ 댓글 삭제 (Soft Delete)
     * - 본인 댓글만 삭제 가능
     * - DB에선 안 지우고, isDeleted = true, content = null 처리
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다"));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 삭제할 수 있습니다.");
        }

        comment.setDeleted(true); // 삭제 표시
        comment.setContent(null); // 내용 제거
    }

    /**
     * ✏️ 댓글 수정
     * - 본인 댓글만 수정 가능
     * - 내용 업데이트 + 수정 시간 갱신
     */
    @Transactional
    public CommentEntity updateComment(Long commentId, String content, Long userId) {
        CommentEntity comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("댓글을 찾을 수 없습니다"));

        if (!comment.getUser().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인의 댓글만 수정할 수 있습니다.");
        }

        comment.setContent(content);
        comment.setUpdatedAt(LocalDateTime.now());

        return comment;
    }

}
