package com.together.documentManger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.together.meeting.MeetingEntity;
import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "file_entity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName; //파일 이름

    private String fileType; //파일 타입

    private String fileSize; //파일 크기

    private String googleDriveFileId; // Google Drive에서의 파일 ID

    private String fileUrl; // Google Drive에서 제공하는 웹 URL

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private UserEntity user; // 파일 업로드한 사용자

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private ProjectEntity project; // 파일이 속한 프로젝트

    private LocalDateTime createdAt; //만든 날짜
    private LocalDateTime updatedAt; //업데이트 된 날짜

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
