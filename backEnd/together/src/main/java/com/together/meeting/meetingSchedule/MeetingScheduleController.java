package com.together.meeting.meetingSchedule;

import com.together.meeting.meetingSchedule.dto.MeetingScheduleRequestDto;
import com.together.meeting.meetingSchedule.dto.MeetingScheduleResponseDto;
import com.together.util.customAnnotation.CurrentProject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/meeting/schedules")
@RequiredArgsConstructor
public class MeetingScheduleController {

    private final MeetingScheduleService scheduleService;

    // 회의 일정 생성
    @PostMapping
    public ResponseEntity<Long> createSchedule(
            @CurrentProject(required = false) Long projectId,
            @RequestBody MeetingScheduleRequestDto requestDto) {
        return ResponseEntity.ok(scheduleService.createSchedule(projectId, requestDto));
    }

    // 특정 프로젝트의 모든 회의 일정 조회
    @GetMapping
    public ResponseEntity<List<MeetingScheduleResponseDto>> getSchedules(@CurrentProject(required = false) Long projectId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByProject(projectId));
    }

    // 회의 일정 수정
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Void> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody MeetingScheduleRequestDto requestDto) {
        scheduleService.updateSchedule(scheduleId, requestDto);
        return ResponseEntity.ok().build();
    }

    // 회의 일정 삭제
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }
}