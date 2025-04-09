package com.together.meeting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MeetingRepository extends JpaRepository<MeetingEntity, Long> {
    List<MeetingEntity> findByProjectProjectId(Long projectId);

}
