package com.together.project;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.comment.CommentController;
import com.together.comment.CommentEntity;
import com.together.documentManger.FileEntity;
import com.together.meeting.MeetingEntity;
import com.together.notice.NoticeEntity;
import com.together.privateNote.PrivateNoteEntity;
import com.together.project.worktask.WorkTaskEntity;
import com.together.user.UserEntity;
import com.together.user.professor.ProfessorEntity;
import com.together.user.professor.feedback.FeedbackEntity;
import com.together.user.student.StudentEntity;
import com.together.vote.entity.VoteEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_entity")
public class ProjectEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId; // PK

    @Column(nullable = false)
    private String title; // 프로젝트

    @Column(name = "image_url")
    private String imageUrl;  // Google Drive 공유 이미지 URL (nullable)

    // [추가] 프로젝트 생성일 (자동 저장, 수정 불가)
    @Column(name = "created_at", updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    private Date createdAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "user_project",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<ProfessorEntity> professors = new ArrayList<>();  // 교수들과 연결

    public void addProfessor(ProfessorEntity professor) {
        this.professors.add(professor);
        if (!professor.getProjects().contains(this)) {
            professor.getProjects().add(this); // 🔁 양방향 유지
        }
    }

    @OneToMany(mappedBy = "mainProject", cascade = CascadeType.ALL)
    private List<StudentEntity> students = new ArrayList<>();  // 학생들과 연결

    // 프로젝트에 팀원 추가 메서드
    public void addUser(UserEntity user) {
        if (user instanceof StudentEntity) {
            StudentEntity student = (StudentEntity) user;

            // ✅ null 방지 처리
            if (this.getStudents() == null) this.setStudents(new ArrayList<>());
            this.getStudents().add(student);

            // ✅ 역방향 연결도 여기서 처리
            if (student.getMainProject() == null) {
                student.setMainProject(this);
            }

        } else if (user instanceof ProfessorEntity) {
            ProfessorEntity professor = (ProfessorEntity) user;
            this.getProfessors().add(professor);
            professor.getProjects().add(this);
        }
    }

    @ManyToOne
    @JoinColumn(name = "leader_user_id")
    private UserEntity leader; //팀장

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<MeetingEntity> meetings = new ArrayList<>(); //미팅

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<FileEntity> files = new ArrayList<>();  // 프로젝트에 속한 파일들

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<NoticeEntity> notices = new ArrayList<>(); //공지사항

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<VoteEntity> votes = new ArrayList<>(); //투표

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<CommentEntity> comments = new ArrayList<>();

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<PrivateNoteEntity> privateNote = new ArrayList<>();

    @OneToMany(mappedBy = "project")
    private List<FeedbackEntity> feedbacks = new ArrayList<>();

    // ✅ 프로젝트에 속한 작업 일정들 (작업/일정 관리용)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkTaskEntity> workTasks = new ArrayList<>();
}
