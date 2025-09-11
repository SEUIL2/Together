package com.together.meeting.meetingSchedule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MeetingScheduleRepository extends JpaRepository<MeetingScheduleEntity, Long> {
    List<MeetingScheduleEntity> findByProject_ProjectId(Long projectId);

    // 상태가 SCHEDULED이고 오늘 날짜보다 이전인 모든 일정을 찾기 위한 쿼리
    List<MeetingScheduleEntity> findByStatusAndScheduleDateBefore(MeetingScheduleStatus status, LocalDate date);
}
