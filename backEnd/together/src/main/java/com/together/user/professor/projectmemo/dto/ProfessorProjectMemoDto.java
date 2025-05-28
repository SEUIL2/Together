package com.together.user.professor.projectmemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 교수 프로젝트 메모 응답 DTO
 * - 클라이언트로 메모 목록 전송 시 사용
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfessorProjectMemoDto {
    private Long id;          // 메모 ID
    private Long projectId;   // 연결된 프로젝트 ID
    private String content;   // 메모 내용
    private Date createdAt;   // 생성일
    private Date updatedAt;   // 수정일
}