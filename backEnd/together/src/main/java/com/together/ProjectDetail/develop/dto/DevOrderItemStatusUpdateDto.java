// backEnd/together/src/main/java/com/together/ProjectDetail/develop/dto/DevOrderItemStatusUpdateDto.java
package com.together.ProjectDetail.develop.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 기능별 개발 순서 '완료 여부' 상태 업데이트 전용 DTO
 */
@Getter
@Setter
public class DevOrderItemStatusUpdateDto {
    private boolean completed; //완료여부
}