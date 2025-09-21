package com.together.ProjectDetail.develop;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DevelopmentEnvironmentRepository extends JpaRepository<DevelopmentEnvironmentEntity, Long> {

    // 개발 언어(devLanguage)에 특정 문자열이 포함된 엔티티를 모두 검색
    List<DevelopmentEnvironmentEntity> findByDevLanguageContainingIgnoreCase(String keyword);

    // [추가] projectId로 연관된 모든 개발 환경 엔티티를 검색
    List<DevelopmentEnvironmentEntity> findAllByProjectEntity_ProjectId(Long projectId);
}