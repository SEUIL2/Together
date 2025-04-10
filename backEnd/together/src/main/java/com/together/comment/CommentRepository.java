package com.together.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {
    // 특정 게시글의 댓글 전체 조회 (작성 순으로)
    List<CommentEntity> findByTargetIdAndCommentTypeOrderByCreatedAtAsc(Long targetId, CommentEntity.CommentType commentType);

}
