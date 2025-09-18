package com.together.meeting.meetingSchedule.dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class MeetingScheduleRequestDto {
    private String title;
    private String description;
    private LocalDateTime scheduleDate;
}