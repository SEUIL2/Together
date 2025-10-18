package com.together.project;

import com.together.ProjectDetail.develop.DevelopmentEnvironmentEntity;
import com.together.user.professor.ProfessorEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final EntityManager em;

    @Override
    public List<ProjectEntity> findProjectsByCriteria(Long professorId, Map<String, String> criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ProjectEntity> cq = cb.createQuery(ProjectEntity.class);

        Root<ProjectEntity> project = cq.from(ProjectEntity.class);
        Join<ProjectEntity, ProfessorEntity> professor = project.join("professors");
        Join<ProjectEntity, DevelopmentEnvironmentEntity> devEnv = project.join("developmentEnvironmentEntities", JoinType.LEFT); // LEFT JOIN으로 변경

        List<Predicate> predicates = new ArrayList<>();

        // 기본 조건: 교수 ID
        predicates.add(cb.equal(professor.get("userId"), professorId));

        // 허용된 개발환경 필드(DevEnv)에 매핑되는 키 목록
        Set<String> allowedDevEnvKeys = Set.of(
                "devLanguage",
                "database",
                "framework",
                "operatingSystem",
                "ide",
                "versionControl",
                "etc"
        );

        // 동적 조건을 모으기 위한 리스트
        List<Predicate> dynamicPredicates = new ArrayList<>();

        // 동적 조건: Map에 담겨온 검색어들을 안전하게 처리
        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            String key = entry.getKey();   // 예: "devLanguage", "database", 또는 "term"
            String value = entry.getValue(); // 예: "Java", "MySQL", 또는 범용 검색어

            if (value == null || value.trim().isEmpty()) {
                continue;
            }

            String lowerPattern = "%" + value.toLowerCase() + "%";

            // 'term' 은 범용 검색어: 프로젝트 제목 + 개발환경의 여러 필드를 OR 조건으로 검색
            if ("term".equalsIgnoreCase(key)) {
                List<Predicate> orPreds = new ArrayList<>();
                // 프로젝트 제목 검색
                orPreds.add(cb.like(cb.lower(project.get("title")), lowerPattern));

                // 개발환경의 허용된 필드들에 대해 OR 검색
                for (String devKey : allowedDevEnvKeys) {
                    orPreds.add(cb.like(cb.lower(devEnv.get(devKey)), lowerPattern));
                }

                dynamicPredicates.add(cb.or(orPreds.toArray(new Predicate[0])));
            } else {
                // 개별 키는 개발환경 필드에 매핑되는 허용된 키들만 처리
                if (!allowedDevEnvKeys.contains(key)) {
                    // 알 수 없는 키면 무시
                    continue;
                }

                dynamicPredicates.add(cb.like(cb.lower(devEnv.get(key)), lowerPattern));
            }
        }

        if (!dynamicPredicates.isEmpty()) {
            predicates.addAll(dynamicPredicates);
        }

        cq.select(project).distinct(true).where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(cq).getResultList();
    }
}