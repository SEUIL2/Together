package com.together.privateNote;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import com.together.user.student.StudentEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "private_note_entity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrivateNoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long noteId;

    @ManyToOne
    @JoinColumn(name = "author", nullable = false)
    private UserEntity author;  // 메모 작성자 (학생 or 교수)

    @ManyToOne
    @JoinColumn(name = "target_student_id", nullable = false)
    private StudentEntity targetStudent;  // 메모 대상 학생

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt = new Date();

    @ManyToOne
    @JoinColumn(name = "project")
    private ProjectEntity project; //메모에 해당하는 프로젝트
}