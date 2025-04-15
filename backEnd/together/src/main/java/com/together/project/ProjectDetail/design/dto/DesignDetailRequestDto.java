package com.together.project.ProjectDetail.design.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class DesignDetailRequestDto {
    private String type;                    // usecase, classDiagram, sequence, ...
    private String text;                    // 텍스트 입력
    private String jsonData;                // 클래스 다이어그램 JSON 저장용
    private List<MultipartFile> files;      // 첨부 파일 목록

    
}