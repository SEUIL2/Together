package com.together.project;

import java.util.List;
import java.util.Map;

public interface ProjectRepositoryCustom {
    /**
     * 여러 검색 조건을 받아 동적으로 프로젝트를 검색하는 메서드
     * @param professorId 검색을 요청한 교수의 ID
     * @param criteria 검색 조건들이 담긴 Map (예: {"devLanguage": "Java", "database": "MySQL"})
     * @return 검색된 프로젝트 엔티티 목록
     */
    List<ProjectEntity> findProjectsByCriteria(Long professorId, Map<String, String> criteria);
}