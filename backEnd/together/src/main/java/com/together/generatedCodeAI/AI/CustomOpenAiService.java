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
     * @param language         - 생성할 언어 : "java-SpringBoot", "nodeJS", "python-FastAPI", "csharp", "kotlin" 를 프론트에서 선택하게 만들어야됌
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
        return String.format("""
            아래는 ERD 정보입니다.
            이 ERD를 바탕으로 %s 백엔드 코드를 생성해주세요, 소스코드만 작성하면 됩니다.

            JSON:
            %s

            코드만 응답해주세요.
            """, lang, json);
    }

}
