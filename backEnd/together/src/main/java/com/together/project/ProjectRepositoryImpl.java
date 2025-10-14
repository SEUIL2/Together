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

        // 동적 조건: Map에 담겨온 검색어들을 AND 조건으로 추가
        for (Map.Entry<String, String> entry : criteria.entrySet()) {
            String key = entry.getKey();   // 예: "devLanguage", "database"
            String value = entry.getValue(); // 예: "Java", "MySQL"

            if (value != null && !value.trim().isEmpty()) {
                // 대소문자 구분 없이 부분 일치 검색 (예: ... LIKE '%java%')
                predicates.add(cb.like(cb.lower(devEnv.get(key)), "%" + value.toLowerCase() + "%"));
            }
        }

        cq.select(project).distinct(true).where(cb.and(predicates.toArray(new Predicate[0])));

        return em.createQuery(cq).getResultList();
    }
}