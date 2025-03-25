package com.together.project.ProjectDetail;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProjectDetailRepository extends JpaRepository<ProjectDetailEntity, Long> {
    Optional<ProjectDetailEntity> findByProject(ProjectEntity project);
}
