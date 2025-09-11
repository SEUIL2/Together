package com.together.meeting.meetingSchedule;

public enum MeetingScheduleStatus {
    SCHEDULED, // 일정이 잡힘
    NEEDS_RESCHEDULING, // 날짜 재설정 필요
    COMPLETED // 완료됨 (선택적으로 추가)
}