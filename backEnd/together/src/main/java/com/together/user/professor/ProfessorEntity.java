package com.together.user.professor;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "professor_entity")
@PrimaryKeyJoinColumn(name = "user_id") //UserEntity 상속
public class ProfessorEntity extends UserEntity {

    @ManyToMany//여러 프로젝트
    private List<ProjectEntity> projects = new ArrayList<>();

    public void addProject(ProjectEntity project) {
        this.projects.add(project);
        if (!project.getProfessors().contains(this)) {
            project.getProfessors().add(this); // 🔁 양방향 유지
        }
    }

}
