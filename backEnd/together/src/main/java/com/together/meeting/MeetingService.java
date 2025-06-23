package com.together.meeting;

import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.user.UserEntity;
import com.together.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Builder
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    // 회의 생성
    @Transactional
    public MeetingEntity createMeeting(MeetingDto meetingDto, Long userId, Long projectId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("프로젝트를 찾을 수 없습니다."));

        MeetingEntity meeting = MeetingEntity.builder()
                .title(meetingDto.getTitle())
                .content(meetingDto.getContent())
                .meetingDate(meetingDto.getMeetingDate())
                .createdAt(meetingDto.getCreatedAt())
                .updatedAt(meetingDto.getUpdatedAt())
                .users(user)
                .project(project)
                .build();

        //미팅 내용 출력될때 userName 을 더하기위해 saved 에 저장
        MeetingEntity saved = meetingRepository.save(meeting);

        return meetingRepository.save(meeting);
    }

    // 회의 전체 목록 조회
    @Transactional(readOnly = true)
    public List<MeetingResponseDto> getAllMeetings(Long projectId) {
        return meetingRepository.findByProjectProjectId(projectId).stream()
                .map(this::convertToDto)
                .toList();
    }

    // 특정 회의 조회
    @Transactional(readOnly = true)
    public MeetingResponseDto getMeetingById(Long meetingId) {
        return meetingRepository.findById(meetingId)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("회의를 찾을 수 없습니다."));
    }

    // 회의 수정
    @Transactional
    public MeetingEntity updateMeeting(Long meetingId, MeetingDto meetingDto, Long userId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("회의를 찾을 수 없습니다."));

        // [삭제] 본인 확인 체크 부분
        // if (!meeting.getUsers().getUserId().equals(userId)) {
        //     throw new IllegalArgumentException("본인이 작성한 회의만 수정할 수 있습니다.");
        // }

        meeting.setTitle(meetingDto.getTitle());
        meeting.setContent(meetingDto.getContent());
        meeting.setMeetingDate(meetingDto.getMeetingDate());
        meeting.setUpdatedAt(meetingDto.getUpdatedAt());

        return meetingRepository.save(meeting);
    }

    // 회의 삭제
    @Transactional
    public void deleteMeeting(Long meetingId, Long userId) {
        MeetingEntity meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new EntityNotFoundException("회의를 찾을 수 없습니다."));

        // 본인이 작성한 회의만 삭제 가능하도록 체크
        if (!meeting.getUsers().getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인이 작성한 회의만 삭제할 수 있습니다.");
        }

        meetingRepository.delete(meeting);
    }

    //UserEntity.userName 추출하기위한 변환 메소드
    private MeetingResponseDto convertToDto(MeetingEntity meeting) {
        return MeetingResponseDto.builder()
                .meetingId(meeting.getMeetingId())
                .title(meeting.getTitle())
                .content(meeting.getContent())
                .meetingDate(meeting.getMeetingDate())
                .createdAt(meeting.getCreatedAt())
                .updatedAt(meeting.getUpdatedAt())
                .authorName(meeting.getUsers().getUserName()) //여기서 userName 을 받아옴
                .build();
    }
}
