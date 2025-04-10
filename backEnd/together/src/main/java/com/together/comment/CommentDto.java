package com.together.comment;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    private Long id;
    private String content;
    private String authorName;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private List<CommentDto> children;
}
