package com.together.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.documentManger.FileEntity;
import com.together.meeting.MeetingEntity;
import com.together.notice.NoticeEntity;
import com.together.project.ProjectEntity;
import com.together.vote.entity.VoteEntity;
import com.together.vote.entity.VoteResponseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public enum UserRole{
        STUDENT, PROFESSOR
    }

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

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
}
