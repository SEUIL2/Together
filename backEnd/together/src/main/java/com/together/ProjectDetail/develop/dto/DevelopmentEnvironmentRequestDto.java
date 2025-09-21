package com.together.ProjectDetail.develop.dto;

import com.together.ProjectDetail.develop.DevelopmentEnvironmentEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DevelopmentEnvironmentRequestDto {

    private String operatingSystem; // 운영체제
    private String ide; // IDE
    private String devLanguage; // 개발 언어
    private String framework; // 프레임워크
    private String versionControl; // 버전관리 시스템
    private String database; // 데이터베이스
    private String etc; // 기타

    // DTO를 Entity로 변환하는 메서드
    public DevelopmentEnvironmentEntity toEntity() {
        DevelopmentEnvironmentEntity entity = new DevelopmentEnvironmentEntity();
        entity.setOperatingSystem(this.operatingSystem);
        entity.setIde(this.ide);
        entity.setDevLanguage(this.devLanguage);
        entity.setFramework(this.framework);
        entity.setVersionControl(this.versionControl);
        entity.setDatabase(this.database);
        entity.setEtc(this.etc);
        return entity;
    }
}