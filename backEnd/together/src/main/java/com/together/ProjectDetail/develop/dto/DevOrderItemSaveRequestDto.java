

package com.together.ProjectDetail.develop.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * '기능별 개발 순서' 항목의 생성(Create) 및 수정(Update) 요청을 처리하기 위한 DTO입니다.
 * 클라이언트로부터 데이터를 '받을' 때 사용됩니다.
 */
@Getter
@Setter
public class DevOrderItemSaveRequestDto {

    /**
     * 기능의 이름입니다.
     */
    private String featureName;

    /**
     * 기능의 개발 순서입니다.
     */
    private Integer featureOrder;

    /**
     * 기능의 중요도입니다.
     */
    private String priority;

    /**
     * 기능에 대한 상세 설명입니다.
     */
    private String description;

    /**
     * 기능의 완료 여부입니다.
     */
    private boolean completed;
}