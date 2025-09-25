package com.together.user.professor.feedback;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackTextHistoryRepository extends JpaRepository<FeedbackTextHistoryEntity, Long> {
    List<FeedbackTextHistoryEntity> findByProfessor_UserIdOrderByUsedAtDesc(Long professorId);
    Optional<FeedbackTextHistoryEntity> findByProfessor_UserIdAndText(Long professorId, String text);
}