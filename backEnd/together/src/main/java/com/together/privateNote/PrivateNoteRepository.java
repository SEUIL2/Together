package com.together.privateNote;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PrivateNoteRepository extends JpaRepository<PrivateNoteEntity, Long> {
    Optional<PrivateNoteEntity> findByAuthorUserIdAndTargetStudentUserId(Long authorId, Long studentId);

}
