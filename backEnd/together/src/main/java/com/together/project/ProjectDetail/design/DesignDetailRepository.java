package com.together.project.ProjectDetail.design;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DesignDetailRepository extends JpaRepository<DesignDetailEntity, Long> {

    // 프로젝트별 설계 상세 정보 조회
    Optional<DesignDetailEntity> findByProject(ProjectEntity project);
}
