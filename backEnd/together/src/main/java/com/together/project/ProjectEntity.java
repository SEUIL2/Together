package com.together.project;

import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "project_entity")
public class ProjectEntity {

    @Column(name = "project_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId; //PK

    @Column
    private String projectName; //프로젝트 이름

    @Column
    private Date projectStartDate; //시작일

    @Column
    private Date projectEndDate; //종료일

    @OneToMany(mappedBy = "project")
    private List<UserEntity> users;
}
