package com.together.vote.DTO;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VoteDetailDTO {
    private Long voteId;
    private String title;
    private Date createdDate;
    private boolean deadlineSelection; //마감기한 On,Off
    private Date deadLine; //마감기한
    private boolean anonymous; //익명 On,Off
    private List<VoteItemResultDTO> items;
}
