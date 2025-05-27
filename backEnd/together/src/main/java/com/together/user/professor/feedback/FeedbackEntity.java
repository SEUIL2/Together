package com.together.user.professor.feedback;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import jdk.jfr.Enabled;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "feedback_entity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long feedbackId;

    private String page;       // 페이지 이름 ('meeting', 'design', ...)
    private int x;             // X 좌표
    private int y;             // Y 좌표
    private String text;       // 피드백 내용

    @CreationTimestamp
    private LocalDateTime createdAt; //만들어진 날짜


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private ProjectEntity project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private UserEntity author; //작성자

    @OneToMany(mappedBy = "feedback")
    private List<FeedbackReadEntity> feedbacks = new ArrayList<>(); //피드백 읽었는지에 대한 엔티티

    private Boolean isRead = false;
}
