package com.together.generatedCodeAI;

import com.together.project.ProjectEntity;
import com.together.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "generated_code_entity")
public class GeneratedCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String codeName; //이름

    private String language; //언어 : "java-SpringBoot", "nodeJS", "python-FastAPI", "csharp", "kotlin"

    @Lob
    private String generatedCode; //생성되는 코드

    @Enumerated(EnumType.STRING)
    private CodeSourceType type; // CLASS_DIAGRAM or ER_DIAGRAM

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author")
    private UserEntity author; //작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project")
    private ProjectEntity project; //해당 프로젝트


}
