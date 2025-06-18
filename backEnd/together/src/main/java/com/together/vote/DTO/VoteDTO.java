package com.together.vote.DTO;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class VoteDTO {

    private String title; // 투표 제목
    private List<String> options; // 투표 항목들 (사용자가 선택할 수 있는 항목들)
    private boolean deadlineSelection; //마감기한 On,Off
    private Date deadLine; //마감기한
    private boolean anonymous; //익명 On,Off

}
