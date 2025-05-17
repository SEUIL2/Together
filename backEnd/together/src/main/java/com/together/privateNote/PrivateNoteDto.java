package com.together.privateNote;

import lombok.Data;

@Data
public class PrivateNoteDto {
    private Long targetStudentId;
    private String content;
}
