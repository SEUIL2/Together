package com.together.documentManger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDto {
    private String fileId;
    private String fileName;
    private String fileUrl;
}