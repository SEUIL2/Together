package com.together.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.comment.CommentEntity;
import com.together.documentManger.FileEntity;
import com.together.meeting.MeetingEntity;
import com.together.notice.NoticeEntity;
import com.together.notification.NotificationEntity;
import com.together.project.ProjectEntity;
import com.together.vote.entity.VoteEntity;
import com.together.vote.entity.VoteResponseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Student 상속
@Table(name = "user_entity")
public class UserEntity {

    @Column(name = "user_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; //PK

    @Column(name = "user_login_id", unique = true)
    private String userLoginId; //로그인 아이디

    @Column
    private String password; //비번

    @Column
    private String userName; //사용자 이름

    @Column(name = "user_email", unique = true)
    private String userEmail; //이메일

    @Column
    private boolean emailVerified; //이메일 인증 여부

    @Column(updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; //임시 유저 더미데이터를 삭제하기 위한 시간 체크 필드

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public enum UserRole{
        STUDENT, PROFESSOR
    }

    @ManyToMany
    @JoinTable(
            name = "user_project", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "user_id"), // 현재 Entity(UserEntity)쪽 FK
            inverseJoinColumns = @JoinColumn(name = "project_id") // 반대쪽 Entity(ProjectEntity) FK
    ) //여러 프로젝트
    private List<ProjectEntity> projects = new ArrayList<>();

    @OneToMany(mappedBy = "users")
    @JsonIgnore
    private List<MeetingEntity> meetings;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FileEntity> files = new ArrayList<>();  // 사용자가 업로드한 파일 리스트

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<NoticeEntity> notices = new ArrayList<>(); // 공지사항

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VoteEntity> votes = new ArrayList<>(); //투표

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VoteResponseEntity> voteResponseEntities = new ArrayList<>(); //투표 Response

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL)
    private List<NotificationEntity> notificationEntities = new ArrayList<>();

    @OneToMany(mappedBy = "user" , cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CommentEntity> commentEntities = new ArrayList<>();

}
