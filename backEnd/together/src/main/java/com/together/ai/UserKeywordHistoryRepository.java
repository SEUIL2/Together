package com.together.ai;

import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserKeywordHistoryRepository extends JpaRepository<UserKeywordHistoryEntity, Long> {
    List<UserKeywordHistoryEntity> findByUser(UserEntity user);
}
