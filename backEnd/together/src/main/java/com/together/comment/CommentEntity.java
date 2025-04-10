package com.together.comment;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment_entity")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content; //내용

    private boolean isDeleted = false; //삭제되었는지에 대한 여부

    private LocalDateTime createdAt; //만든 날짜

    private LocalDateTime updatedAt; //수정 날짜

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private CommentEntity parent; //댓글 (부모)

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentEntity> children = new ArrayList<>(); // // 자식 댓글들 (재귀 구조)

    private Long targetId; //댓글 위치

    @Enumerated(EnumType.STRING)
    private CommentType commentType; //댓글 위치에 대한 타입

    public enum CommentType {
        NOTICE, VOTE, TASK
    } //공지사항 , 투표 , 할일

}
