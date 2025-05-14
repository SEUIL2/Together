// com.together.vote.DTO.VoteListDTO
package com.together.vote.DTO;

import lombok.Data;
import java.util.Date;

@Data
public class VoteListDTO {
    private Long voteId;       // 투표 ID
    private String title;      // 투표 제목
    private Date createdDate;  // 생성 일시
    private String userName;   // 작성자 이름
}
