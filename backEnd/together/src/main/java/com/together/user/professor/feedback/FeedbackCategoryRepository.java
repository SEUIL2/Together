package com.together.user.professor.feedback;

import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackCategoryRepository extends JpaRepository<FeedbackCategoryEntity, Long> {
    List<FeedbackCategoryEntity> findAllByCreatedBy(UserEntity user);
}