package com.together.systemConfig.AOP;

import com.together.comment.CommentEntity;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.professor.ProfessorEntity;
import com.together.user.student.StudentEntity;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

@Aspect
@Component
public class ProjectIdAspect {
    //유저에서 프로젝트 아이디를 받을때 교수의 경우엔 프론트에서 가져오고
    // 학생의 경우엔 자동으로 할당된 프로젝트에서 아이디를 추출하는 구조

    // @Before: 해당 메서드 실행 전에 AOP가 실행됨
    // AOP가 적용될 메서드를 정의합니다. @PostMapping이 붙은 메서드에서 실행됩니다.
    @Before("(@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping)|| " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping)||" +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)) " +
            "&& args(projectId, userDetails, ..)")
    public void setProjectId(Long projectId, UserDetailsImpl userDetails) {

        // 교수일 경우는 프로젝트 아이디를 그대로 사용하고,
        if (userDetails.getUser() instanceof ProfessorEntity) {
            // 교수는 이미 projectId를 프론트에서 받을 수 있도록
            return;
        }

        // 학생일 경우는 mainProject에서 자동으로 가져옴
        if (userDetails.getUser() instanceof StudentEntity) {
            projectId = ((StudentEntity) userDetails.getUser()).getMainProject().getProjectId();
        }

        // 이제 projectId는 AOP에서 자동으로 설정됨. 나머지 변수들은 그대로
    }

}
