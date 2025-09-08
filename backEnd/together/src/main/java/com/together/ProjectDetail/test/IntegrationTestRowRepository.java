package com.together.ProjectDetail.test;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IntegrationTestRowRepository extends JpaRepository<IntegrationTestRowEntity, Long> {
    List<IntegrationTestRowEntity> findByProject(ProjectEntity project);
}
