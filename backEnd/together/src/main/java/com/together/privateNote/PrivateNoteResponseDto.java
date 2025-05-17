package com.together.privateNote;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivateNoteResponseDto {
    private Long noteId; //PK
    private Long targetStudentId; //작성 타켓 학생 ID
    private String targetStudentName; // 작성 타겟 학생 이름
    private Long authorId; //메모 작성자 ID
    private String authorName;
    private String content; //내용
    private Date createdAt; //만든 날짜
}
