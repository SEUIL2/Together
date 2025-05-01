package com.together.vote;

import com.together.systemConfig.UserDetailsImpl;
import com.together.util.customAnnotation.CurrentProject;
import com.together.vote.DTO.VoteDTO;
import com.together.vote.DTO.VoteResponseDTO;
import com.together.vote.entity.VoteEntity;
import com.together.vote.entity.VoteResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/votes")  // "/votes" 경로로 들어오는 모든 요청을 처리
public class VoteController {

    @Autowired
    private VoteService voteService;

    /**
     * 투표 생성 요청 예시:
     * {
     *     "title": "졸업작품 개발 방향 투표",  // 투표 제목
     *     "options": ["프론트엔드 개발", "백엔드 개발"]  // 투표 항목들
     * }
     * @AuthenticationPrincipal - 자동 추출
     * @param voteDTO - 투표 데이터 (제목, 항목들)
     * @return ResponseEntity<VoteEntity> - 생성된 투표 객체 반환
     */
    @PostMapping("/create")
    public ResponseEntity<VoteEntity> createVote(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody VoteDTO voteDTO) {
        Long userId = userDetails.getUser().getUserId();

        VoteEntity createdVote = voteService.createVote(userId, projectId, voteDTO);
        if (createdVote != null) {
            log.info("투표 생성 성공");
            return ResponseEntity.ok(createdVote);  // 투표 생성 성공
        }
        return ResponseEntity.badRequest().body(null);  // 투표 생성 실패
    }

    /**
     * 특정 프로젝트에 속한 모든 투표 조회
     * AuthenticationPrincipal - 자동 추출
     * @return ResponseEntity<List<VoteEntity>> - 프로젝트에 속한 모든 투표 리스트 반환
     */
    @GetMapping("/project")
    public ResponseEntity<List<VoteEntity>> getVotesByProject(
            @CurrentProject(required = false) Long projectId,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {

        List<VoteEntity> votes = voteService.getVotesByProject(projectId);
        return ResponseEntity.ok(votes);  // 투표 목록 반환
    }

    /**
     * 특정 투표 조회
     * @param voteId - 조회할 투표 ID
     * @return ResponseEntity<VoteEntity> - 특정 투표 객체 반환
     */
    @GetMapping("/{voteId}")
    public ResponseEntity<VoteEntity> getVoteById(@PathVariable Long voteId) {
        VoteEntity vote = voteService.getVoteById(voteId);
        if (vote != null) {
            return ResponseEntity.ok(vote);  // 투표 존재하면 반환
        }
        return ResponseEntity.notFound().build();  // 투표가 없으면 404 반환
    }

    /**
     * 투표 제목 수정
     * @param voteId - 수정할 투표 ID
     * @param voteDTO - 수정할 투표 데이터 (제목, 항목 등)
     * @return ResponseEntity<VoteEntity> - 수정된 투표 객체 반환
     */
    @PutMapping("/{voteId}/update")
    public ResponseEntity<VoteEntity> updateVote(@PathVariable Long voteId, @RequestBody VoteDTO voteDTO) {
        VoteEntity updatedVote = voteService.updateVote(voteId, voteDTO);
        if (updatedVote != null) {
            return ResponseEntity.ok(updatedVote);  // 수정 성공
        }
        return ResponseEntity.notFound().build();  // 투표가 없으면 404 반환
    }

    /**
     * 특정 투표 삭제
     * @param voteId - 삭제할 투표 ID
     * @return ResponseEntity<Void> - 삭제 성공 여부에 따라 204 (삭제 성공) 또는 404 (투표 없음) 반환
     */
    @DeleteMapping("/{voteId}/delete")
    public ResponseEntity<Void> deleteVote(@PathVariable Long voteId) {
        boolean isDeleted = voteService.deleteVote(voteId);
        if (isDeleted) {
            log.info("투표 삭제 성공");
            return ResponseEntity.noContent().build();  // 삭제 성공
        }
        return ResponseEntity.notFound().build();  // 삭제할 투표가 없으면 404 반환
    }

    /**
     * 사용자가 투표 응답을 생성하는 API
     * 투표 응답 예시:
     * {
     *     "user": 3, // 응답한 사용자 ID
     *     "voteItemId": 1 // 선택한 투표 항목 ID
     * }
     * @param voteId - 응답을 달고자 하는 투표 ID
     * @param voteResponseDTO - 투표 응답 데이터 (사용자 ID, 투표 항목 ID)
     * @return ResponseEntity<?> - 생성된 응답 객체 반환
     * 중복 응답시 409
     */
    @PostMapping("/{voteId}/response")
    public ResponseEntity<?> createVoteResponse(@PathVariable Long voteId, @RequestBody VoteResponseDTO voteResponseDTO) {
        try {
            VoteResponseEntity voteResponse = voteService.createVoteResponse(voteResponseDTO.getUserId(), voteId, voteResponseDTO.getVoteItemId());
            if (voteResponse != null) {
                log.info("투표 응답 성공");
                return ResponseEntity.ok(voteResponse);  // 응답 생성 성공
            }
        }catch (IllegalStateException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage()); // 409 Conflict
        }
        return ResponseEntity.badRequest().body(null);  // 응답 생성 실패
    }
}
