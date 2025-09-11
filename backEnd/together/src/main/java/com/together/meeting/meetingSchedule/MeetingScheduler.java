package com.together.meeting.meetingSchedule;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MeetingScheduler {

    private final MeetingScheduleService scheduleService;

    // 매일 자정에 실행 (cron = "초 분 시 일 월 요일")
    @Scheduled(cron = "0 0 0 * * *")
    public void checkForOverdueMeetings() {
        scheduleService.updateOverdueSchedules();
    }
}