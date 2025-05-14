package com.together.vote.DTO;

import lombok.Data;

@Data
public class VoteItemResultDTO {
    private Long voteItemId;
    private String options;
    private int responseCount;
}
