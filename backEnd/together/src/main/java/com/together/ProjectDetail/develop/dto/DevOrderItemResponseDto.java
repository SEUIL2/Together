// backEnd/together/src/main/java/com/together/ProjectDetail/develop/dto/DevOrderItemResponseDto.java
package com.together.ProjectDetail.develop.dto;

import com.together.ProjectDetail.develop.DevOrderItemEntity;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;
import lombok.Setter;

/**
 * 기능별 개발 순서 조회 응답 DTO
 */
@Getter
@Setter
public class DevOrderItemResponseDto {

    private Long id;
    private String featureName;
    private Integer featureOrder;
    private String priority;
    private String description;
    private boolean completed;
    private String authorName;

    // 엔티티를 DTO로 변환
    public static DevOrderItemResponseDto fromEntity(DevOrderItemEntity devOrderItemEntity) {
        DevOrderItemResponseDto dto = new DevOrderItemResponseDto();
        dto.setId(devOrderItemEntity.getId());
        dto.setFeatureName(devOrderItemEntity.getFeatureName());
        dto.setFeatureOrder(devOrderItemEntity.getFeatureOrder());
        dto.setPriority(devOrderItemEntity.getPriority());
        dto.setDescription(devOrderItemEntity.getDescription());
        dto.setCompleted(devOrderItemEntity.isCompleted());

        // --- 작성자 조회 오류를 해결하기 위해 이 부분을 수정했습니다 ---
        try {
            // 연결된 작성자가 있는지 확인하고 이름을 설정합니다.
            if (devOrderItemEntity.getAuthor() != null) {
                dto.setAuthorName(devOrderItemEntity.getAuthor().getUserName());
            } else {
                dto.setAuthorName("작성자 없음");
            }
        } catch (EntityNotFoundException e) {
            // author_id는 있지만 실제 UserEntity가 없는 경우 (옛날 데이터)
            // 오류를 발생시키는 대신 기본값을 설정합니다.
            dto.setAuthorName("작성자 정보 없음");
        }
        // --- 여기까지 수정 ---

        return dto;
    }
}