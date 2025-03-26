package com.together.vote.DTO;

import lombok.Data;

@Data
public class VoteResponseDTO {

    private Long voteItemId;  // 선택된 투표 항목 ID
    private Long userId;  // 사용자 ID

}
