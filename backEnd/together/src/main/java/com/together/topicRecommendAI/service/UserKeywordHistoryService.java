package com.together.topicRecommendAI.service;

import com.together.topicRecommendAI.UserKeywordHistoryEntity;
import com.together.topicRecommendAI.UserKeywordHistoryRepository;
import com.together.user.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 주제 추천 시 사용된 키워드 히스토리를 관리하는 서비스입니다.
 * 키워드 저장, 조회, 개수 제한 로직을 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class UserKeywordHistoryService {

    private final UserKeywordHistoryRepository historyRepository;

    // 히스토리 최대 저장 개수 (50개)
    private static final int MAX_HISTORY_COUNT = 40;

    /**
     * 새로운 키워드 히스토리를 저장하고, 50개 제한을 초과하면 가장 오래된 항목을 삭제합니다.
     * @param user 키워드를 사용한 사용자
     * @param keyword 저장할 키워드
     */
    @Transactional // 데이터 저장 및 삭제가 하나의 트랜잭션으로 동작
    public void addKeywordHistory(UserEntity user, String keyword) {

        // 1. 새로운 키워드 히스토리 생성 및 저장
        UserKeywordHistoryEntity newHistory = new UserKeywordHistoryEntity();
        newHistory.setUser(user);
        newHistory.setKeyword(keyword);
        // createdAt은 Entity에서 기본값으로 자동 생성됩니다.

        historyRepository.save(newHistory);

        // 2. 현재 사용자의 총 히스토리 개수 확인
        long totalCount = historyRepository.countByUser(user); // [cite] 1번 항목에서 수정한 Repository 사용

        // 3. 50개를 초과하는지 확인
        if (totalCount > MAX_HISTORY_COUNT) {
            // 4. 초과하는 개수 계산
            int excessCount = (int) (totalCount - MAX_HISTORY_COUNT);

            // 5. 가장 오래된 항목(createdAt 오름차순)을 초과하는 개수만큼 조회
            List<UserKeywordHistoryEntity> oldestEntries =
                    historyRepository.findByUserOrderByCreatedAtAsc(user, PageRequest.of(0, excessCount)); // [cite] 1번 항목에서 수정한 Repository 사용

            // 6. 조회된 가장 오래된 항목들 삭제
            if (!oldestEntries.isEmpty()) {
                historyRepository.deleteAll(oldestEntries);
            }
        }
    }

    /**
     * [추가] 특정 사용자의 모든 이전 키워드를 조회합니다.
     * (AiKeywordGenerator의 getPreviousKeywords 메소드 로직을 이관)
     * @param user 사용자 엔티티
     * @return 키워드 문자열 리스트
     */
    @Transactional(readOnly = true) // 읽기 전용 트랜잭션
    public List<String> getPreviousKeywords(UserEntity user) {
        return historyRepository.findByUser(user).stream() //
                .map(UserKeywordHistoryEntity::getKeyword) //
                .collect(Collectors.toList());
    }
}