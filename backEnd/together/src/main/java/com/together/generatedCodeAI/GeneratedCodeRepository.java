package com.together.generatedCodeAI;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GeneratedCodeRepository extends JpaRepository<GeneratedCodeEntity,Long> {
    List<GeneratedCodeEntity> findByProject(ProjectEntity project);

}
