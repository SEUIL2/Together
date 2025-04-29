package com.together.user.student;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "student_entity")
@PrimaryKeyJoinColumn(name = "user_id") //UserEntity 상속
public class StudentEntity extends UserEntity {

    @Column(unique = true)
    private String studentNumber; //학번

    @ManyToOne
    @JoinColumn(name = "main_project_id")
    private ProjectEntity mainProject; //대표 프로젝트

}
