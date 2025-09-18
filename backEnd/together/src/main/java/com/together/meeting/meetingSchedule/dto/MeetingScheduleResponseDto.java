package com.together.meeting.meetingSchedule.dto;

import com.together.meeting.meetingSchedule.MeetingScheduleStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class MeetingScheduleResponseDto {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime scheduleDate;
    private MeetingScheduleStatus status;

    public static MeetingScheduleResponseDto fromEntity(com.together.meeting.meetingSchedule.MeetingScheduleEntity schedule) {
        return MeetingScheduleResponseDto.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .description(schedule.getDescription())
                .scheduleDate(schedule.getScheduleDate())
                .status(schedule.getStatus())
                .build();
    }
}