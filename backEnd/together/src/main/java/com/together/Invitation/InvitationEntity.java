package com.together.Invitation;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "invitation")
public class InvitationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "status") // 'PENDING', 'ACCEPTED', 'REJECTED'
    private String status = "PENDING";  // 기본값: 대기 중

    @Column(nullable = false)
    private boolean accepted = false; // 초대 수락 여부 (기본값 false)

    public void acceptInvitation() {
        this.accepted = true;
    }
}
