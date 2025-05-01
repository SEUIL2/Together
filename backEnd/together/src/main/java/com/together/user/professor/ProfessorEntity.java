package com.together.user.professor;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToMany
    @JoinTable(
            name = "user_project", // 중간 테이블 이름
            joinColumns = @JoinColumn(name = "user_id"), // 현재 Entity(UserEntity)쪽 FK
            inverseJoinColumns = @JoinColumn(name = "project_id") // 반대쪽 Entity(ProjectEntity) FK
    ) //여러 프로젝트
    private List<ProjectEntity> projects = new ArrayList<>();

}
