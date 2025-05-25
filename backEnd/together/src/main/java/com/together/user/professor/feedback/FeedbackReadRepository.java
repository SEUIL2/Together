package com.together.user.professor.feedback;

import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackReadRepository extends JpaRepository<FeedbackReadEntity, Long> {
    boolean existsByUserAndFeedback(UserEntity user, FeedbackEntity feedback);
    List<FeedbackReadEntity> findAllByUser(UserEntity user);
    Optional<FeedbackReadEntity> findByUserAndFeedback(UserEntity user, FeedbackEntity feedback);

}
