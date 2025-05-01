package com.together.util.customAnnotation;

import com.together.project.ProjectEntity;

//AOP 에서 사용하는 ThreadLocal
public class ProjectContext {

    private static final ThreadLocal<ProjectEntity> projectHolder = new ThreadLocal<>();
    private static final ThreadLocal<Long> projectIdHolder = new ThreadLocal<>();

    public static void setProject(ProjectEntity project) {
        projectHolder.set(project);
        projectIdHolder.set(project.getProjectId());
    }

    public static ProjectEntity getProject() {
        return projectHolder.get();
    }

    public static Long getProjectId() {
        return projectIdHolder.get();
    }

    public static void clear() {
        projectHolder.remove();
        projectIdHolder.remove();
    }

}
