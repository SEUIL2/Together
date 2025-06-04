package com.together.vote.DTO;

import lombok.Data;

import java.util.List;

@Data
public class VoteItemResultDTO {
    private Long voteItemId;
    private String options;
    private int responseCount;
    private List<String> voterNames; // 투표자
}
