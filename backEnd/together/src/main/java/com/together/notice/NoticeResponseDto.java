package com.together.notice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class NoticeResponseDto {

    private Long noticeId;

    private String title;
    private String content;
    private Date createdDate;

    private String authorName;

}
