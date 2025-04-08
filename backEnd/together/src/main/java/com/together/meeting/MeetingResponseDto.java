package com.together.meeting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class MeetingResponseDto {

    private Long meetingId;

    private String title;

    private String content;

    private LocalDateTime meetingDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String authorName;

}
