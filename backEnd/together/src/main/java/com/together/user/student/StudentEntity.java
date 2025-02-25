package com.together.user.student;

import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@PrimaryKeyJoinColumn(name = "user_id") //UserEntity 상속
public class StudentEntity extends UserEntity {

    @Column(unique = true)
    private String studentNumber; //학번
}
