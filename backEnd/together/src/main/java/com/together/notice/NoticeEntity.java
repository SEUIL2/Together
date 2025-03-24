package com.together.notice;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notice_entity") //공지사항 엔티티
public class NoticeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId; // PK

    private String title; // 공지사항 제목

    @Column(length = 1000)
    private String content; // 공지사항 내용

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date(); // 작성 날짜

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user; // 작성자 (UserEntity와 연관)

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    private ProjectEntity project;

}
