package com.together.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_entity")
public class UserEntity {

    @Column(name = "user_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId; //PK

    @Column(unique = true)
    private Long loginId; //로그인 아이디

    @Column
    private String password; //비번

    @Column
    private String userName; //사용자 이름

    @Column
    private String userEmail; //이메일

    enum role{
        student, professor
    }
}
