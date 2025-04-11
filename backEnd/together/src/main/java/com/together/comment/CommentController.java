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

    /**
     * 📌 댓글 조회 API (트리 형태로 리턴됨)
     * ✅ 프론트에서 댓글 목록을 가져올 때 사용하는 API
     * <p>
     * 🔸 PathVariable 설명
     * @param type     - 댓글이 달린 곳 종류 (NOTICE, VOTE, TASK 중 하나)
     * @param targetId - 댓글이 달린 대상의 ID (예: 공지사항 ID가 3이면 → /comments/NOTICE/3)
     *
     * 🔁 응답: List<CommentDto> 트리 구조로 정렬된 댓글 리스트
     */
    @GetMapping("/{type}/{targetId}")
    public ResponseEntity<List<CommentDto>> getComments(
            @PathVariable("type") CommentEntity.CommentType type,
            @PathVariable("targetId") Long targetId) {
        return ResponseEntity.ok(commentService.getCommentTree(targetId, type));
    }

    /**
     * 📝 댓글 작성 API
     * ✅ 프론트에서 새로운 댓글을 작성할 때 사용
     * <p>
     * 📤 전송 데이터
     * - Body (text/plain 또는 JSON 형태): 댓글 내용만 전송
     *   예시: "이 공지에 대해 질문이 있습니다"
     * <p>
     * 🔸 PathVariable 설명
     * @param type     - 댓글이 달린 곳 종류 (NOTICE, VOTE, TASK 중 하나)
     * @param targetId - 댓글이 달린 대상 ID (예: 공지 ID = 3이면 /comments/NOTICE/3)
     * <p>
     * 🔸 RequestParam 설명
     * @param parentId - 대댓글일 경우 부모 댓글 ID (없으면 null 또는 생략)
     * <p>
     * 🔒 @AuthenticationPrincipal: 백엔드에서 로그인한 사용자 정보 자동 추출
     *
     * 🔁 응답: 성공 메시지 "댓글이 등록되었습니다"
     */
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

    /**
     * ✏️ 댓글 수정 API
     * ✅ 프론트에서 본인의 댓글 내용을 수정할 때 사용
     * <p>
     * 🔸 PathVariable 설명
     * @param commentId - 수정할 댓글 ID
     * <p>
     * 📤 Body: 수정된 댓글 내용을 보내야 함
     *   예시: "내용을 수정했어요"
     *
     * 🔁 응답: 성공 메시지 "댓글이 수정되었습니다"
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable Long commentId,
            @RequestBody String content,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        commentService.updateComment(commentId, content, userId);
        return ResponseEntity.ok("댓글이 수정되었습니다");
    }

    /**
     * 🗑️ 댓글 삭제 API
     * ✅ 프론트에서 본인의 댓글을 삭제할 때 사용
     * <p>
     * 🔸 PathVariable 설명
     * @param commentId - 삭제할 댓글 ID
     *
     * 🔁 응답: 성공 메시지 "댓글이 삭제되었습니다"
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long userId = userDetails.getUser().getUserId();
        commentService.deleteComment(commentId, userId);
        return ResponseEntity.ok("댓글이 삭제되었습니다");
    }

}
