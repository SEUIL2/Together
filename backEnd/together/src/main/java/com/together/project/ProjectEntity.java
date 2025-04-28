package com.together.project;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.comment.CommentController;
import com.together.comment.CommentEntity;
import com.together.documentManger.FileEntity;
import com.together.meeting.MeetingEntity;
import com.together.notice.NoticeEntity;
import com.together.user.UserEntity;
import com.together.vote.entity.VoteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_entity")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "project_id")
    private Long projectId; // PK

    @Column(nullable = false)
    private String title; // 프로젝트 이름

    @ManyToMany(mappedBy = "projects")
    private List<UserEntity> users = new ArrayList<>();

    // 프로젝트에 팀원 추가 메서드
    public void addUser(UserEntity user) {
        users.add(user); // 내 프로젝트(users) 리스트에 유저 추가
        user.getProjects().add(this); // 유저의 projects 리스트에도 이 프로젝트 추가
    }

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<MeetingEntity> meetings; //미팅

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<FileEntity> files = new ArrayList<>();  // 프로젝트에 속한 파일들

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<NoticeEntity> notices = new ArrayList<>(); //공지사항

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<VoteEntity> votes = new ArrayList<>(); //투표

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CommentEntity> commentEntities = new ArrayList<>();


}
