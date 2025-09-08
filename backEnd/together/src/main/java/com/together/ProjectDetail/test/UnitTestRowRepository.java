package com.together.ProjectDetail.test;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UnitTestRowRepository extends JpaRepository<UnitTestRowEntity, Long> {
    List<UnitTestRowEntity> findByProject(ProjectEntity project);
}
