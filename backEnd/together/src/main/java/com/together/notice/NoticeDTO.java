package com.together.notice;

import lombok.Data;

import java.util.Date;

@Data
public class NoticeDTO {

    private Long noticeId;

    private String title;
    private String content;
    private Date createdDate;

    private Long userId;  // 작성자 ID
    private Long projectId;  // 프로젝트 ID
}
