package com.together.user.professor.feedback;

import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback_text_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackTextHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private UserEntity professor;

    @Column(nullable = false, length = 1000)
    private String text;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime usedAt;
}