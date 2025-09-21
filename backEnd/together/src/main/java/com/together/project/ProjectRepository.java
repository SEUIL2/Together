package com.together.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;
import java.util.List;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {
    // [추가] 교수가 담당하고, 특정 개발 언어를 사용하는 프로젝트 목록 검색
    @Query("SELECT DISTINCT p FROM ProjectEntity p JOIN p.professors prof JOIN p.developmentEnvironmentEntities d WHERE prof.userId = :professorId AND d.devLanguage LIKE %:language%")
    List<ProjectEntity> findProjectsByProfessorAndLanguage(
            @Param("professorId") Long professorId,
            @Param("language") String language
    );
}
