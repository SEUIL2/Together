package com.together.project.ProjectDetail.test;

import com.together.project.ProjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 테스트 표 행(TestRowEntity) 레파지토리
 */
@Repository
public interface TestRowRepository extends JpaRepository<TestRowEntity, Long> {

    // 특정 프로젝트의 모든 테스트 행(단위/통합 전체)
    List<TestRowEntity> findByProject(ProjectEntity project);

    // 특정 프로젝트 + 타입별(단위/통합) 테스트 행만 조회
    List<TestRowEntity> findByProjectAndTableType(ProjectEntity project, TestTableType tableType);
}
