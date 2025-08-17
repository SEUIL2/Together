package com.together.worktask;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * WorkTaskEntity CRUD 및 프로젝트별 조회를 담당하는 레포지토리.
 */
@Repository
public interface WorkTaskRepository extends JpaRepository<WorkTaskEntity, Long> {

    /**
     * 특정 프로젝트에 속한 모든 작업 조회
     * @param project 프로젝트 엔티티
     * @return 작업 리스트
     */
    List<WorkTaskEntity> findByProject(ProjectEntity project);
    List<WorkTaskEntity> findByProject_ProjectId(Long projectId);
}
