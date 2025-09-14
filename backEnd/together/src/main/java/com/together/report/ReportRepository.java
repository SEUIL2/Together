
package com.together.report;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * 보고서 DB 작업을 위한 리포지토리
 */
public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    // 프로젝트 ID로 모든 보고서 조회
    List<ReportEntity> findByProject_ProjectId(Long projectId);

    // 특정 사용자가 작성한 모든 보고서 조회
    List<ReportEntity> findByAuthor_UserId(Long userId);
}