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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL)
    private List<FeedbackReadEntity> feedbacks = new ArrayList<>(); //피드백 읽었는지에 대한 엔티티

    private Boolean isRead = false;

//    @Enumerated(EnumType.STRING)
//    @Column(nullable = false)
//    private FeedbackCategory category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "feedback_selected_categories",
            joinColumns = @JoinColumn(name = "feedback_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<FeedbackCategoryEntity> categories = new HashSet<>(); // 변경 및 추가
}
