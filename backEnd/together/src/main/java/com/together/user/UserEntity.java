package com.together.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.generatedCodeAI.GeneratedCodeEntity;
import com.together.report.ReportEntity;
import com.together.worktask.WorkTaskEntity;
import com.together.topicRecommendAI.UserKeywordHistoryEntity;
import com.together.comment.CommentEntity;
import com.together.documentManger.FileEntity;
import com.together.meeting.MeetingEntity;
import com.together.notice.NoticeEntity;
import com.together.notification.NotificationEntity;
import com.together.privateNote.PrivateNoteEntity;
import com.together.project.ProjectEntity;
import com.together.user.professor.feedback.FeedbackEntity;
import com.together.user.professor.feedback.FeedbackReadEntity;
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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserKeywordHistoryEntity> userKeywordHistoryEntities = new ArrayList<>();


    /**
     * 프로필 사진 (Google Drive URL 등)
     */
    @Column(name = "profile_image_url", nullable = true)
    private String profileImageUrl;

    /**
     * 자기소개 (한 줄 소개 등)
     */
    @Column(name = "bio", length = 200)
    private String bio;

    /**
     * 사용자 테마 (어두운 테마 / 밝은 테마)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "theme", nullable = false)
    private ThemeOption theme = ThemeOption.LIGHT; // 기본값

    public enum ThemeOption {
        LIGHT,
        DARK
    }

    @OneToMany(mappedBy = "leader")
    private List<ProjectEntity> project = new ArrayList<>(); //프로젝트

    @OneToMany(mappedBy = "author")
    private List<PrivateNoteEntity> users = new ArrayList<>(); //메모

    @OneToMany(mappedBy = "author")
    private List<FeedbackEntity> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<FeedbackReadEntity> feedbackRead = new ArrayList<>();

    @OneToMany(mappedBy = "assignedUser")
    private List<WorkTaskEntity> assignedUsers = new ArrayList<>();

    @OneToMany(mappedBy = "author")
    private List<GeneratedCodeEntity> generatedCodes = new ArrayList<>();

    /**
     * 이 사용자가 작성한 보고서 목록
     */
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReportEntity> reports = new ArrayList<>();


}
