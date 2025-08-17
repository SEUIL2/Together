package com.together.ProjectDetail.develop;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DevelopDetailRepository extends JpaRepository<DevelopDetailEntity, Long> {
    Optional<DevelopDetailEntity> findByProject(ProjectEntity project);
}