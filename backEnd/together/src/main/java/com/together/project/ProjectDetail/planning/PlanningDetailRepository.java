package com.together.project.ProjectDetail.planning;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlanningDetailRepository extends JpaRepository<PlanningDetailEntity, Long> {
    Optional<PlanningDetailEntity> findByProject(ProjectEntity project);
}
