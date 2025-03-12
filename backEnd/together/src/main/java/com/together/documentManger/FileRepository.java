package com.together.documentManger;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

    List<FileEntity> findByProject(ProjectEntity project);

    List<FileEntity> findByUser(UserEntity user);

    Optional<FileEntity> findByGoogleDriveFileId(String googleDriveFileId);

}
