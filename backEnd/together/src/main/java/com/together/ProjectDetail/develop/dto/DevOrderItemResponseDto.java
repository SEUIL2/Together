// backEnd/together/src/main/java/com/together/ProjectDetail/develop/dto/DevOrderItemResponseDto.java

package com.together.ProjectDetail.develop.dto;

import com.together.ProjectDetail.develop.DevOrderItemEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * '기능별 개발 순서' 항목의 조회 결과를 클라이언트로 '보내기' 위한 DTO입니다.
 * 서버의 응답(Response) 데이터로 사용됩니다.
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

    /**
     * DevOrderItem 엔티티를 DevOrderItemResponseDto로 변환하는 정적 팩토리 메서드입니다.
     * 서비스 로직에서 변환을 쉽게 처리할 수 있도록 도와줍니다.
     * @param devOrderItem 엔티티 객체
     * @return 변환된 DTO 객체
     */
    public static DevOrderItemResponseDto fromEntity(DevOrderItemEntity devOrderItem) {
        DevOrderItemResponseDto dto = new DevOrderItemResponseDto();
        dto.setId(devOrderItem.getId());
        dto.setFeatureName(devOrderItem.getFeatureName());
        dto.setFeatureOrder(devOrderItem.getFeatureOrder());
        dto.setPriority(devOrderItem.getPriority());
        dto.setDescription(devOrderItem.getDescription());
        dto.setCompleted(devOrderItem.isCompleted());
        return dto;
    }
}
