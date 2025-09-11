package com.together.meeting.meetingSchedule;

import com.together.meeting.meetingSchedule.dto.MeetingScheduleRequestDto;
import com.together.meeting.meetingSchedule.dto.MeetingScheduleResponseDto;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingScheduleService {

    private final MeetingScheduleRepository scheduleRepository;
    private final ProjectRepository projectRepository; // 프로젝트 정보를 가져오기 위해

    // 회의 일정 생성 (Create)
    @Transactional
    public Long createSchedule(Long projectId, MeetingScheduleRequestDto requestDto) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        MeetingScheduleEntity schedule = new MeetingScheduleEntity();
        schedule.setProject(project);
        schedule.setTitle(requestDto.getTitle());
        schedule.setDescription(requestDto.getDescription());
        schedule.setScheduleDate(requestDto.getScheduleDate());
        schedule.setStatus(MeetingScheduleStatus.SCHEDULED);

        MeetingScheduleEntity savedSchedule = scheduleRepository.save(schedule);
        return savedSchedule.getId();
    }

    // 특정 프로젝트의 모든 회의 일정 조회 (Read)
    public List<MeetingScheduleResponseDto> getSchedulesByProject(Long projectId) {
        return scheduleRepository.findByProject_ProjectId(projectId).stream()
                .map(MeetingScheduleResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    // 회의 일정 수정 (Update)
    @Transactional
    public void updateSchedule(Long scheduleId, MeetingScheduleRequestDto requestDto) {
        MeetingScheduleEntity schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found"));

        schedule.setTitle(requestDto.getTitle());
        schedule.setDescription(requestDto.getDescription());
        schedule.setScheduleDate(requestDto.getScheduleDate());
        // 날짜를 수정하면 다시 SCHEDULED 상태로 변경
        schedule.setStatus(MeetingScheduleStatus.SCHEDULED);
    }

    // 회의 일정 삭제 (Delete)
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new IllegalArgumentException("Schedule not found");
        }
        scheduleRepository.deleteById(scheduleId);
    }

    // 날짜가 지난 회의 일정 상태 업데이트 (스케줄러가 호출)
    @Transactional
    public void updateOverdueSchedules() {
        List<MeetingScheduleEntity> overdueSchedules = scheduleRepository.findByStatusAndScheduleDateBefore(
                MeetingScheduleStatus.SCHEDULED, LocalDate.now()
        );
        for (MeetingScheduleEntity schedule : overdueSchedules) {
            schedule.setStatus(MeetingScheduleStatus.NEEDS_RESCHEDULING);
        }
    }
}