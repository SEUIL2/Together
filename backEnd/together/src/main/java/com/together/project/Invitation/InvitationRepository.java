package com.together.project.Invitation;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {
    List<InvitationEntity> findByUser(UserEntity user);
    Optional<InvitationEntity> findByProjectAndUser(ProjectEntity project, UserEntity user);

}