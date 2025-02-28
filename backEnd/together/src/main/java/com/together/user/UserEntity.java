package com.together.user;

import com.together.project.ProjectEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // Student 상속
@Table(name = "user_entity")
public class UserEntity {

    @Column(name = "user_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; //PK

    @Column(name = "user_login_id", unique = true)
    private String userLoginId; //로그인 아이디

    @Column
    private String password; //비번

    @Column
    private String userName; //사용자 이름

    @Column(name = "user_email", unique = true)
    private String userEmail; //이메일

    @Column
    @Enumerated(EnumType.STRING)
    private UserRole role;

    public enum UserRole{
        STUDENT, PROFESSOR
    }

    @ManyToOne
    @JoinColumn(name = "project_id")
    private ProjectEntity project;
}
