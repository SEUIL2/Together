package com.together.user.professor.projectmemo;

import com.together.project.ProjectEntity;
import com.together.user.professor.ProfessorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 교수 프로젝트 메모 Repository
 * - 특정 교수 또는 특정 프로젝트 기준으로 메모 목록 조회 가능
 */
public interface ProfessorProjectMemoRepository extends JpaRepository<ProfessorProjectMemoEntity, Long> {

    // 📌 특정 프로젝트의 전체 메모 조회
    List<ProfessorProjectMemoEntity> findByProject(ProjectEntity project);

    // 📌 특정 교수가 남긴 모든 메모 조회
    List<ProfessorProjectMemoEntity> findByProfessor(ProfessorEntity professor);
}