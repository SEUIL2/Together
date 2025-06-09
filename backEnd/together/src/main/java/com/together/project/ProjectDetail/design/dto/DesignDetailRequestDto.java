package com.together.project.ProjectDetail.design.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class DesignDetailRequestDto {
    private String type;             // usecase, classDiagram, erd 등 항목명
    private String text;             // 설명 텍스트
    private String json;             // JSON 데이터 (클래스다이어그램, 유스케이스, ERD 등)
    private List<MultipartFile> files; // 첨부파일 리스트


    
}