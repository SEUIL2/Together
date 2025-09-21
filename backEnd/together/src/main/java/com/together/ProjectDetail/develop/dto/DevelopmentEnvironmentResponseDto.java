package com.together.ProjectDetail.develop.dto;

import com.together.ProjectDetail.develop.DevelopmentEnvironmentEntity;
import lombok.Getter;

@Getter
public class DevelopmentEnvironmentResponseDto {

    private final Long id;
    private final String operatingSystem; // 운영체제
    private final String ide; // IDE
    private final String devLanguage; // 개발 언어
    private final String framework; // 프레임워크
    private final String versionControl; // 버전관리 시스템
    private final String database; // 데이터베이스
    private final String etc; // 기타

    // Entity를 DTO로 변환하는 생성자
    public DevelopmentEnvironmentResponseDto(DevelopmentEnvironmentEntity entity) {
        this.id = entity.getId();
        this.operatingSystem = entity.getOperatingSystem();
        this.ide = entity.getIde();
        this.devLanguage = entity.getDevLanguage();
        this.framework = entity.getFramework();
        this.versionControl = entity.getVersionControl();
        this.database = entity.getDatabase();
        this.etc = entity.getEtc();
    }
}