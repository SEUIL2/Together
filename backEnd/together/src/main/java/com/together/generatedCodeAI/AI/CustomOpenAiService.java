package com.together.generatedCodeAI.AI;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOpenAiService {

    private final OpenAiApiClient openAiClient;

    /**
     * 클래스 다이어그램 JSON 기반 코드 생성 요청
     *
     * @param classDiagramJson - 저장된 클래스 다이어그램 JSON
     * @param language         - 생성할 언어 : "java-SpringBoot", "node.js-express", "python-FastAPI", "csharp", "kotlin" 를 프론트에서 선택하게 만들어야됌
     * @return AI가 응답한 생성된 코드 문자열
     */
    public String generateFromClassDiagram(String classDiagramJson, String language) {
        String prompt = buildClassPrompt(classDiagramJson, language);
        return openAiClient.ask(prompt);
    }

    /**
     * ER 다이어그램 JSON 기반 코드 생성 요청
     *
     * @param erdJson - 저장된 ERD JSON
     * @param language - 생성할 언어
     * @return 생성된 코드 (AI 응답)
     */
    public String generateFromErd(String erdJson, String language) {
        String prompt = buildErdPrompt(erdJson, language);
        return openAiClient.ask(prompt);
    }

    /**
     * 클래스 다이어그램용 프롬프트 구성 함수
     *
     * @param json - 클래스 다이어그램 JSON
     * @param lang - 언어 종류
     * @return AI에 전달할 prompt 텍스트
     */
    private String buildClassPrompt(String json, String lang) {
        return String.format("""
            아래는 클래스 다이어그램 JSON입니다.
            이 다이어그램을 바탕으로 %s 백엔드 코드를 생성해주세요, 소스코드만 작성하면 됩니다.

            JSON:
            %s

            코드만 응답해주세요.
            """, lang, json);
    }

    /**
     * ERD 용 프롬프트 구성 함수
     *
     * @param json - ERD JSON
     * @param lang - 언어 종류
     * @return AI에 전달할 prompt 텍스트
     */
    private String buildErdPrompt(String json, String lang) {

        String instruction;

        switch (lang.toLowerCase()) {
            case "java-springboot" -> instruction = """
            아래는 ERD 정보입니다.
            이 ERD를 기반으로 Java(Spring Boot)에서 사용할 JPA Entity 클래스만 생성해주세요.
            - Lombok(@Data, @Entity 등)을 사용하세요.
            - 관계 설정이 필요하다면 @OneToMany, @ManyToOne 등을 포함해주세요.
            - Repository, Service, Controller는 작성하지 마세요.
            """;

            case "node.js-express" -> instruction = """
            아래는 ERD 정보입니다.
            이 ERD를 기반으로 **Node.js + Express 환경에서 사용할 Mongoose 모델(schema) 코드**만 생성해주세요.
            
            - 각 테이블에 해당하는 **Mongoose 스키마(schema)** 를 정의해주세요.
            - 필요한 경우 관계 설정(`ref`)도 포함해주세요.
            - **라우터, 컨트롤러, 서비스 코드 등은 작성하지 마세요.**
            - **모든 코드는 JavaScript 기반이며 CommonJS 형식(require/module.exports)**을 사용해주세요.
            - 가독성을 위해 들여쓰기와 주석을 적절히 사용해주세요.
            """;

            case "python-fastapi" -> instruction = """
            아래는 ERD 정보입니다.
            이 ERD를 기반으로 Python FastAPI에서 사용할 Pydantic 모델만 생성해주세요.
            - SQLAlchemy 모델이나 라우팅 코드는 포함하지 마세요.
            - 각 테이블에 대응하는 Pydantic BaseModel만 작성해주세요.
            """;

            case "csharp" -> instruction = """
            아래는 ERD 정보입니다.
            이 ERD를 기반으로 C# Entity 클래스만 생성해주세요.
            - Entity Framework Core 기준으로 작성해주세요.
            - Navigation property 포함하고, Controller/Service 등은 생략해주세요.
            """;

            case "kotlin" -> instruction = """
            아래는 ERD 정보입니다.
            이 ERD를 기반으로 Kotlin에서 사용할 JPA Entity 클래스만 생성해주세요.
            - Spring Boot와 Kotlin을 기준으로, data class로 작성해주세요.
            - Lombok은 사용하지 않고 Kotlin의 기본 문법을 활용해주세요.
            - 기타 구성요소는 작성하지 말고 Entity만 응답해주세요.
            """;

            default -> instruction = String.format("""
            아래는 ERD 정보입니다.
            이 ERD를 바탕으로 %s 백엔드 코드를 생성해주세요.
            소스코드만 응답해주세요.
            """, lang);
        }

        return String.format("""
        %s

        JSON:
        %s

        코드만 응답해주세요.
        """, instruction, json);
    }

}
