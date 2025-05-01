/*
package com.together.systemConfig.AOP;

import com.together.comment.CommentEntity;
import com.together.project.ProjectEntity;
import com.together.project.ProjectRepository;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.professor.ProfessorEntity;
import com.together.user.student.StudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Aspect
@Component
public class ProjectIdAspect {
    //유저에서 프로젝트 아이디를 받을때 교수의 경우엔 프론트에서 가져오고
    // 학생의 경우엔 자동으로 할당된 프로젝트에서 아이디를 추출하는 구조

    @Autowired
    private ProjectRepository projectRepository;

    // @Before: 해당 메서드 실행 전에 AOP가 실행됨
    // AOP가 적용될 메서드를 정의합니다. @PostMapping이 붙은 메서드에서 실행됩니다.
    @Before("(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)|| " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping)||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)) " +
            "&& args(projectId, userDetails, ..)")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public void setProjectId(Long projectId, UserDetailsImpl userDetails) {
        log.info("✅ AOP setProjectId 실행됨! projectId = " + projectId + ", user = " + userDetails.getUsername());

        if (userDetails.getUser() instanceof ProfessorEntity) { //교수는 이미 projectId를 프론트에서 받을 수 있도록그대로 저장
            // 교수는 전달된 projectId 사용
        } else if (userDetails.getUser() instanceof StudentEntity) { // 학생일 경우는 mainProject에서 자동으로 가져옴
            projectId = ((StudentEntity) userDetails.getUser()).getMainProject().getProjectId();
            log.info("👉 학생이라 mainProject에서 가져온 ID: " + projectId);
        }

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트 없음"));

        ProjectContext.setProject(project);
        log.info("✅ Context 저장 완료! projectId = " + project.getProjectId());

        // 이제 projectId는 AOP에서 자동으로 설정됨. 나머지 변수들은 그대로
    }

    @After("execution(* com.together..*Controller.*(..))")
    public void clearProjectContext() {
        ProjectContext.clear(); // ✅ memory leak 방지!
    }

}
*/
