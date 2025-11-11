package com.together.topicRecommendAI;

import com.together.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface UserKeywordHistoryRepository extends JpaRepository<UserKeywordHistoryEntity, Long> {
    List<UserKeywordHistoryEntity> findByUser(UserEntity user);

    /**
     * [추가] 특정 사용자의 키워드 히스토리 총 개수를 조회합니다.
     * @param user 사용자 엔티티
     * @return 키워드 개수
     */
    long countByUser(UserEntity user);

    /**
     * [추가] 특정 사용자의 키워드 히스토리를 생성 시간(createdAt) 오름차순으로 정렬하여
     * Pageable (페이지 크기) 만큼 조회합니다. (즉, 가장 오래된 항목부터 조회)
     * @param user 사용자 엔티티
     * @param pageable (e.g., PageRequest.of(0, 10)) -> 0번째 페이지에서 10개
     * @return 가장 오래된 키워드 히스토리 리스트
     */
    List<UserKeywordHistoryEntity> findByUserOrderByCreatedAtAsc(UserEntity user, Pageable pageable);
}
