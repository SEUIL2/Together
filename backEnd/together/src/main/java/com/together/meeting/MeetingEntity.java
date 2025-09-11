package com.together.meeting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "meeting_entity")
public class MeetingEntity {

    @Column(name = "meeting_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long meetingId; //PK

    private String title; // 회의 제목

    private String content; // 회의 내용

    private LocalDateTime meetingDate; // 회의 날짜

    private LocalDateTime createdAt; // 생성 날짜

    private LocalDateTime updatedAt; // 수정 날짜

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private ProjectEntity project;
}
