package com.together.comment;

import com.together.systemConfig.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글 목록 조회 (트리 형태)
    @GetMapping("/{type}/{targetId}")
    public ResponseEntity<List<CommentDto>> getComments(
            @PathVariable("type") CommentEntity.CommentType type,
            @PathVariable("targetId") Long targetId) {
        return ResponseEntity.ok(commentService.getCommentTree(targetId, type));
    }

    // 댓글 작성
    @PostMapping("/{type}/{targetId}")
    public ResponseEntity<?> createComment(
            @PathVariable("type") CommentEntity.CommentType type,
            @PathVariable("targetId") Long targetId,
            @RequestParam(required = false) Long parentId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        Long projectId = userDetails.getUser().getProject().getProjectId();
        Long userId = userDetails.getUser().getUserId();
        commentService.createComment(content, userId, projectId, targetId, type, parentId);
        return ResponseEntity.ok("댓글이 등록되었습니다");
    }

    //댓글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        commentService.updateComment(commentId, content, userId);
        return ResponseEntity.ok("댓글이 수정되었습니다");
    }

    //댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 삭제되었습니다");
    }

}
