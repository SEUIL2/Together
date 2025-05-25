package com.together.user.professor.feedback;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<FeedbackEntity, Long> {
    List<FeedbackEntity> findByProject_ProjectIdAndPage(Long projectId, String page);
    List<FeedbackEntity> findAllByProjectIn(List<ProjectEntity> projects);

    List<FeedbackEntity> findAllByAuthor(UserEntity user);

    List<FeedbackEntity> findAllByProject_ProjectIdAndAuthor(Long projectId, UserEntity user);
    Optional<FeedbackEntity> findByFeedbackIdAndProject_ProjectId(Long feedbackId, Long projectId);
}
