package com.together.user.student;

import com.together.privateNote.PrivateNoteEntity;
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
@Table(name = "student_entity")
@PrimaryKeyJoinColumn(name = "user_id") //UserEntity 상속
public class StudentEntity extends UserEntity {

    @Column(unique = true)
    private String studentNumber; //학번

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "main_project_id")
    private ProjectEntity mainProject; //대표 프로젝트

    @Column(name = "user_color", nullable = true)
    private String userColor; //유저 색 프로젝트 나가면 NULL초기화

    @OneToMany(mappedBy = "targetStudent")
    private List<PrivateNoteEntity> privateNotes = new ArrayList<>(); //메모

}
