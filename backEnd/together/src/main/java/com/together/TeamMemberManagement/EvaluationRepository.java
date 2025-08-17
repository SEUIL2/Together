package com.together.TeamMemberManagement;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvaluationRepository extends JpaRepository<EvaluationEntity, Long> {
    List<EvaluationEntity> findByProject(ProjectEntity project);
    List<EvaluationEntity> findByEvaluatee(UserEntity evaluatee);
}
