package com.together.vote.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "vote_entity")
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vote_id")
    private Long voteId; // PK

    private String title;  // 투표 제목

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate = new Date(); // 생성 날짜

    private Date deadLine = new Date(); //마감 기한

    private boolean deadlineSelection; //마감기한 On,Off

    private boolean anonymous; //익명 On,Off

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @ToString.Exclude
    private UserEntity user; // 작성자 (유저)

    @ManyToOne
    @JoinColumn(name = "project_id")
    @JsonIgnore
    @ToString.Exclude
    private ProjectEntity project; // 프로젝트

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteItemEntity> voteItems = new ArrayList<>(); // 투표 항목들

    @OneToMany(mappedBy = "vote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VoteResponseEntity> voteResponseEntitys = new ArrayList<>();; //투표 Response


}
