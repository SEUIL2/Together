package com.together.meeting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingDto {

    private Long meetingId; //PK

    private String title; // 회의 제목

    private String content; // 회의 내용

    private LocalDateTime meetingDate; // 회의 날짜

    private LocalDateTime createdAt; // 생성 날짜

    private LocalDateTime updatedAt; // 수정 날짜

    private Long userId; // 유저 ID만 저장

}
