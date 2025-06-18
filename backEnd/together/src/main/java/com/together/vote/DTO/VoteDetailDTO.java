package com.together.vote.DTO;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VoteDetailDTO {
    private Long voteId;
    private String title;
    private Date createdDate;
    private List<VoteItemResultDTO> items;
    private String userName;
}
