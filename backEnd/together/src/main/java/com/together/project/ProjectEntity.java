package com.together.project;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.meeting.MeetingEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_entity")
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "project_id")
    private Long projectId; // PK

    @Column(nullable = false)
    private String title; // 프로젝트 이름

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMM-yyyy hh:mm:ss a", timezone = "Asia/Seoul")
    @Temporal(TemporalType.DATE)
    @Column(name = "start_date", nullable = true)
    private Date projectStartDate; // 시작일

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MMM-yyyy hh:mm:ss a", timezone = "Asia/Seoul")
    @Temporal(TemporalType.DATE)
    @Column(name = "end_date", nullable = true)
    private Date projectEndDate; // 종료일

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserEntity> users = new ArrayList<>();

    // 프로젝트에 팀원 추가 메서드
    public void addUser(UserEntity user) {
        users.add(user);
        user.setProject(this);
    }

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<MeetingEntity> meetings; //m

    // 프로젝트에서 팀원 제거 메서드
    public void removeUser(UserEntity user) {
        users.remove(user);
        user.setProject(null);
    }

   /* private Long projectId; //PK

    @Column
    private String projectName; //프로젝트 이름

    @Column
    private Date projectStartDate; //시작일

    @Column
    private Date projectEndDate; //종료일

    @OneToMany(mappedBy = "project")
    private List<UserEntity> users;

    @OneToMany(mappedBy = "project")
    @JsonIgnore
    private List<MeetingEntity> meetings;
     */

}
