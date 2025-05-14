package com.together.vote.DTO;

import lombok.Data;

import java.util.List;

@Data
public class VoteDetailDTO {
    private Long voteId;
    private String title;
    private List<VoteItemResultDTO> items;
}
