package com.together.util.customAnnotation;

import com.together.project.ProjectEntity;
import com.together.systemConfig.UserDetailsImpl;
import com.together.user.professor.ProfessorEntity;
import com.together.user.student.StudentEntity;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentProjectArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentProject.class)
                && (parameter.getParameterType().equals(ProjectEntity.class)
                || parameter.getParameterType().equals(Long.class));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {

        CurrentProject annotation = parameter.getParameterAnnotation(CurrentProject.class);
        boolean required = annotation.required();

        // 🔥 AuthenticationPrincipal에서 직접 꺼냄
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Long projectId = null;

        if (userDetails.getUser() instanceof ProfessorEntity) {
            // 교수는 프론트에서 직접 넣어야 함 → 파라미터 확인
            String rawId = webRequest.getParameter("projectId");
            if (rawId != null) {
                projectId = Long.parseLong(rawId);
            }
        } else if (userDetails.getUser() instanceof StudentEntity student) {
            ProjectEntity mainProject = student.getMainProject();
            if (mainProject != null) {
                projectId = mainProject.getProjectId();
            }
        }

        if (projectId == null && required) {
            throw new IllegalStateException("프로젝트 ID를 찾을 수 없습니다.");
        }

        return projectId;
    }

}
