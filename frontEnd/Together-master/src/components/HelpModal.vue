<template>
  <transition name="modal-fade">
    <div class="modal-overlay" @click.self="$emit('close')">
      <div class="modal-container">
        <div class="modal-header">
          <h2 class="modal-title">📘 예시 가이드</h2>
          <button class="close-btn" @click="$emit('close')">✕</button>
        </div>

        <!-- 대분류 탭 -->
        <div class="category-tabs">
          <button
            v-for="category in Object.keys(helpData)"
            :key="category"
            :class="{ active: selectedCategory === category }"
            @click="selectCategory(category)"
          >
            {{ category }}
          </button>
        </div>

        <!-- 하위 항목 버튼 -->
        <div class="subcategory-buttons">
          <button
            v-for="item in helpData[selectedCategory]"
            :key="item.title"
            :class="{ active: selectedSubItem?.title === item.title }"
            @click="selectSubItem(item)"
          >
            {{ item.title }}
          </button>
        </div>

        <!-- 설명 및 이미지 영역 -->
        <div v-if="selectedSubItem" class="item-content">
          <div class="description-box">
            <p>{{ selectedSubItem.description }}</p>
          </div>
          <img
            v-if="selectedSubItem.image"
            :src="selectedSubItem.image"
            loading="lazy"
            alt="예시 이미지"
            class="help-image"
          />
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref } from 'vue'
import requirment from '@/assets/helpimage/requirment.png'
import infostructure from '@/assets/helpimage/infostructure.png'
import sequence from '@/assets/helpimage/sequence.png'
import table from '@/assets/helpimage/table.png'
import storyboard from '@/assets/helpimage/storyboard.png'
import system from '@/assets/helpimage/system.png'
import uidesign from '@/assets/helpimage/uidesign.png'
import usecase from '@/assets/helpimage/usecase.png'
import classdiagram from '@/assets/helpimage/classdiagram.png'
import erd from '@/assets/helpimage/erd.png'
import test from '@/assets/test.png'

// 선택된 카테고리 및 항목
const selectedCategory = ref('기획')
const selectedSubItem = ref(null)

const selectCategory = (category) => {
  selectedCategory.value = category
  selectedSubItem.value = helpData[category][0] || null
}
const selectSubItem = (item) => {
  selectedSubItem.value = item
}

// 예시 데이터 (이미지는 원하는 URL로 교체)
const helpData = {
  기획: [
    {
      title: "프로젝트 동기",
      description: `이 프로젝트를 시작하게 된 계기와 배경을 설명합니다.

예시 :
    "현대 사회에서 팀 프로젝트는 필수적인 협업 역량을 요구하지만, 구성원 간 소통 부재나 역할 분담의 어려움으로 인해 프로젝트의 완성도가 떨어지는 경우가 많습니다. 
    본 프로젝트는 이러한 문제를 해결하기 위해, 팀원 간 효과적인 커뮤니케이션과 작업 관리를 지원하는 플랫폼을 개발하고자 시작되었습니다."`,
    },
    {
      title: "프로젝트 목표",
      description: `프로젝트가 이루고자 하는 구체적인 목표를 명시합니다.
      
예시 :
    "본 프로젝트의 목표는 팀원 간의 원활한 소통과 효율적인 작업 분담을 지원하는 통합 플랫폼을 구축하여,
    사용자들이 팀 프로젝트를 보다 체계적이고 성공적으로 수행할 수 있도록 돕는 것입니다."`,
    },
    {
      title: "요구사항 정의",
      description: "요구사항 정의서는 시스템이 제공해야 할 기능을 표 형태로 정리해, 각 기능의 식별자(ID)와 이름, 수행 내용, 필요 데이터(필수·선택)를 한눈에 볼 수 있게 하는 문서입니다. 이를 통해 개발자·기획자·테스터 등 모든 이해관계자가 동일한 기준으로 요구사항을 파악하고, 설계·구현·검증 단계에서 참고할 수 있습니다.",
      image: requirment
    },
        {
      title: "정보구조도",
      description: "정보 구조도는 웹·앱 서비스의 전체 화면(또는 페이지)과 메뉴, 주요 기능 간 계층 및 네비게이션 흐름을 시각적으로 정리한 다이어그램입니다. 이를 통해 기획자·디자이너·개발자·테스터 등 모든 이해관계자는 사용자 여정(유저 플로우)과 정보 배치 구조를 한눈에 파악하고, 화면 설계·UI/UX 디자인·개발·검증 단계에서 일관된 기준으로 작업을 진행할 수 있습니다.",
      image: infostructure
    },
        {
      title: "스토리보드",
      description: `스토리보드는 서비스 화면 간 전환과 사용자 조작 흐름을 순차적으로 시각화한 다이어그램입니다.
각 단계별 화면 구성과 주요 버튼·메뉴 동작을 한눈에 파악할 수 있어,
기획자·디자이너·개발자·테스터가 일관된 기준으로 UI/UX를 설계·구현·검증할 때 활용됩니다.`,
      image: storyboard
    },
  ],
  설계: [
    {
      title: "유스케이스 다이어그램",
      description: `유스케이스 다이어그램은 시스템과 외부 주체(액터) 간의 상호작용을 ‘유스케이스(기능)’로 모델링하여, 누가 어떤 기능을 수행·제공하는지를 시각화한 UML 다이어그램입니다.이를 통해 시스템의 범위와 주요 기능, 액터 간 관계(«include», «extend» 등)를 명확히 정의하고, 요구사항 분석 및 설계 초기 단계에서 기획자·개발자·테스터 등 이해관계자 간 공통 이해를 돕습니다.`,
      image: usecase
    },
    {
      title: "클래스 다이어그램",
      description: "클래스 다이어그램은 객체 지향 설계에서 시스템의 구조를 시각적으로 나타내는 중요한 도구입니다. 이 다이어그램은 시스템 내의 클래스들 간의 관계를 정의하며, 클래스가 가진 속성(attributes)과 메소드(methods)를 포함합니다. 또한, 클래스 간의 상속, 연관, 집합 관계 등을 통해 시스템의 객체들 간의 상호작용을 명확히 할 수 있습니다. 클래스 다이어그램은 UML(통합 모델링 언어) 표기법을 따르며, 시스템 분석 및 설계 단계에서 중요한 역할을 합니다. 이를 통해 개발자는 시스템의 구조를 효과적으로 이해하고, 객체 간의 관계를 추적할 수 있습니다.",
      image: classdiagram
    },
        {
      title: "시퀀스 다이어그램",
      description: "시퀀스 다이어그램은 시스템을 구성하는 객체(또는 액터)들 사이에서 주고받는 메시지 흐름을 시간 순서대로 표현한 UML 다이어그램입니다. 이를 통해 특정 기능이 실행되는 과정에서 누가 언제 어떤 요청을 보내고, 어떤 응답을 받아 최종 결과를 만들어내는지를 단계별로 명확히 파악할 수 있어 설계·개발·테스트 시 상호작용 로직을 검증하고 문서화하는 데 활용됩니다",
      image: sequence
    },
        {
      title: "UI 디자인",
      description: "UI 디자인 시안은 실제 구현될 화면의 레이아웃·색상·타이포그래피·아이콘·컴포넌트 배치 등을 시각적으로 구체화한 문서(혹은 목업)입니다. 이를 통해 기획자·디자이너·개발자·테스터가 동일한 기준으로 화면을 구현하고 사용자 경험을 일관되게 유지할 수 있습니다.",
      image: uidesign
    },
        {
      title: "ER 다이어그램",
      description: "ERD(Entity-Relationship Diagram)는 데이터베이스 설계에서 사용되는 시각적 도구로, 데이터 간의 관계를 모델링하는 데 사용됩니다. ERD는 엔터티(Entity), 속성(Attribute), 그리고 엔터티 간의 관계(Relationship)를 나타내며, 이를 통해 데이터베이스 구조를 명확하게 정의할 수 있습니다. 엔터티는 시스템에서 중요한 객체나 개념을 나타내고, 속성은 해당 엔터티의 특성이나 정보를 나타냅니다. 관계는 두 엔터티 간의 상호작용이나 연결을 설명하며, 관계의 종류는 1:1, 1:N, N:M 등으로 구분됩니다. ERD는 데이터베이스 설계의 기초를 제공하며, 데이터 모델링 과정에서 중요한 역할을 합니다.",
      image: erd
    },
        {
      title: "테이블 명세서",
      description: `테이블 명세서는 데이터베이스에 생성될 테이블의 구조를 상세히 기록한 문서입니다. 주요 내용으로는 프로젝트·서비스 정보(시스템명, 작성자, 작성일 등), DBMS·스키마·테이블명 및 설명, 컬럼 목록(컬럼명, 타입, 길이, PK·FK·NN 제약, 기본값, 정의/설명, 참조 테이블 등), 인덱스 정보(인덱스명, 타입, 유니크 여부, 구성 컬럼), 데이터 보존·백업 정책(보존 기간, 백업 주기) 등을 포함합니다.
이를 통해 데이터 모델링 단계 이후 개발자·DBA·테스터 간에 동일한 기준으로 테이블을 생성·검증하고, 운영 중에도 스키마 변경이나 데이터 관리 시 참고할 수 있습니다.
`,
      image: table
    },
        {
      title: "시스템 아키텍쳐",
      description: `시스템 아키텍처 다이어그램은 서비스의 주요 컴포넌트(애플리케이션 서버, 데이터베이스, 캐시, CDN, 로드밸런서 등)와 이들 간의 배포·연결 관계, 네트워크·포트 구성, CI/CD 파이프라인 흐름을 시각화한 문서입니다. 
이를 통해 인프라·개발·운영팀은 전체 시스템의 구조를 한눈에 이해하고, 설계·구현·배포·모니터링 단계에서 공통의 기준으로 활용할 수 있습니다.
`,
      image: system
    },
    //     {
    //   title: "프로젝트 일정 계획",
    //   description: "화면 흐름 및 UI 구성을 나타냅니다.",
    //   image: "https://via.placeholder.com/400x200?text=화면설계"
    // },
  ],
  개발: [
    {
      title: "개발 환경 설정",
      description: `
      개발 환경 설정 문서는 팀원 누구나 동일한 환경에서 프로젝트를 빌드·실행할 수 있도록 필수 도구와 설정 과정을 정리한 가이드입니다. 
      보통 다음 항목으로 구성됩니다.

      사전 준비(PREREQUISITES)
        - 운영체제(OS) 버전
        - JDK(Java) 또는 Node.js, Python 등 런타임 버전
        - IDE(예: IntelliJ, VS Code) 및 플러그인

      의존성 설치(DEPENDENCIES)
        - 빌드 도구(Gradle/Maven, npm/yarn) 사용법
        - 라이브러리 설치 명령 (./gradlew build, npm install 등)

      환경 변수 및 설정(Configuration)
        - .env 또는 application.yml 샘플 파일
        - DB 접속 정보(URL, 계정, 비밀번호)
        - Redis, 메시지큐, 외부 API 키 등

      데이터베이스 초기화(DB INIT)
        - 스키마 생성 및 마이그레이션 명령(Flyway, Liquibase 등)
        - 샘플 데이터(더미 데이터) 삽입 절차

      실행 및 디버깅(RUN & DEBUG)
        - 로컬 실행 명령(java -jar, npm start 등)
        - 포트 구성 및 테스트 엔드포인트
        - IDE에서의 디버깅 설정

      추가 도구 및 스크립트(OPTIONAL)
        - 코드 스타일 검사(ESLint, Checkstyle)
        - 포맷터(Prettier, EditorConfig)
        - CI/CD 연동(GitHub Actions, Jenkins)

이 가이드를 따라 환경을 맞추면, 프로젝트 실행 오류를 최소화하고 협업 효율을 높일 수 있습니다.
`,
    },
    {
      title: "기능별 개발 순서",
      description: `
      기능별 개발 순서는 프로젝트의 요구사항을 기능 단위로 나누고, 우선순위에 따라 개발 순서를 정하는 과정입니다. 
      이를 통해 팀은 핵심 기능을 먼저 구현하여 빠르게 프로토타입을 만들고, 점진적으로 서비스를 완성해 나갈 수 있습니다.

      1. 기능 정의 (Epic & User Story)
        - 가장 큰 기능 단위(Epic)를 정의하고, 각 Epic을 사용자 관점의 작은 기능(User Story)으로 나눕니다.

      2. 우선순위 설정 (Priority)
        - 각 기능의 중요도, 긴급도, 개발 난이도를 고려하여 우선순위를 정합니다. (예: P1 - 필수, P2 - 중요, P3 - 보통)

      3. 담당자 배정 (Assignee)
        - 각 기능별로 담당 개발자를 지정하여 책임과 역할을 명확히 합니다.

      4. 상태 관리 (Status)
        - 각 기능의 진행 상태(To Do, In Progress, Done)를 추적하여 팀 전체의 개발 현황을 공유합니다.

      예시:
        - P1 (Epic: 사용자 관리)
          - 회원가입 기능 (담당자: 김개발) - In Progress
          - 로그인 기능 (담당자: 김개발) - To Do
        - P2 (Epic: 게시판)
          - 게시글 목록 조회 (담당자: 박코딩) - To Do
          - 게시글 작성 기능 (담당자: 박코딩) - To Do
      `,
    },
        {
      title: "커밋 메시지 규칙",
      description: `
      커밋 메시지는 프로젝트 이력을 명확히 남기고, 협업 시 변경 내용을 빠르게 이해하기 위해 일관된 형식을 따르는 것이 중요합니다.
      다음은 Conventional Commits 기반의 권장 규칙을 ‘중간 길이’로 정리한 예시입니다.

      1. 전체 구조
	    <type>(<scope>): <subject> 
	    <body>
	    <footer> 

      2. 제목 줄(Subject)
        - 형식: type(scope): 요약문
        - type: feat, fix, docs, style, refactor, perf, test, chore 중 하나
        - scope(선택): 변경 대상 모듈 또는 컴포넌트 이름
        - subject: 소문자 명령형·50자 이내·마침표 생략

      3. 본문(Body)
        - 변경 이유와 구현 방식을 상세히 설명
        - “왜(Why)”와 “어떻게(How)”를 중심으로 기술
        - 72자 내외로 줄 바꿈

      4. 푸터(Footer)
        - 이슈 자동 닫기: Closes #123
        - 호환성을 깨뜨리는 변경 시:
            | BREAKING CHANGE: API 응답 데이터 구조 변경 

      5. 예시
	    <type>(<scope>): <subject> =  feat(auth): 로그인 시 리프레시 토큰 자동 갱신 기능 추가 
	    <body> 		                              =  액세스 토큰 만료 시 사용자가 재로그인하지 않도록 백그라운드에서 리프레시 토큰으로 갱신 처리 
				                                      - 관련 유닛 테스트 추가 
	    <footer>		                              =  Closes #42 

      `,
      image: "https://via.placeholder.com/400x200?text=핵심기능"
    },
        {
      title: "폴더 구조 및 파일 규칙",
      description: `
      /프로젝트 루트
         |
        ├─ README.md, LICENSE, .gitignore
         |
        ├─ docs/          # 설계·API·운영 문서  
         |
        ├─ config/        # .env.example 등 환경별 설정
         |
        ├─ scripts/       # build.sh, deploy.sh 등  
         |
        ├─ src/           # 애플리케이션 소스  
         |
        ├─ tests/         # 단위·통합 테스트  
         |
        ├─ assets/        # 이미지·폰트·스타일  
         |
        └─ tools/         # 개발·검증 도구  

      파일·폴더명 규칙
        - 폴더: 소문자, 하이픈(-)·언더바(_) 사용
        - 소스: 언어별 관례 따름
          | Java/C#: PascalCase.java
          | JS/TS: camelCase.js, 컴포넌트는 PascalCase.tsx
          | Python: snake_case.py
        - 테스트: 원본과 경로 동일, *.spec.js, *_test.py 등
        - 설정: .env.example, .env.development, .env.production
        - 문서: docs/api-spec.md 등 소문자+하이픈
        - 스크립트: 동사형 이름, 실행권한 부여(chmod +x)

      `,
      image: "https://via.placeholder.com/400x200?text=핵심기능"
    },
  ],
  테스트: [
    {
      title: "단위 테스트 및 통합 테스트",
      description: `테스트 페이지는 프로젝트의 품질과 안정성을 보장하기 위해 '단위 테스트'와 '통합 테스트'를 체계적으로 기록하고 관리하는 공간입니다.
개발한 기능들이 개별적으로 잘 작동하는지, 그리고 여러 기능이 합쳐졌을 때도 충돌 없이 올바르게 동작하는지 이곳에서 검증하고 이력을 남길 수 있습니다.`,
      image: test
    }
  ]
}

// 초기 선택
selectSubItem(helpData[selectedCategory.value][0])
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1001;
}

.modal-container {
  background: #ffffff;
  border-radius: 12px;
  width: 80%;
  height: 90%;
  overflow-y: auto;
  padding: 24px 32px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  position: relative;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

/* --- Transition --- */
.modal-fade-enter-active,
.modal-fade-leave-active {
  transition: opacity 0.3s ease;
}
.modal-fade-enter-from,
.modal-fade-leave-to {
  opacity: 0;
}
.modal-fade-enter-from .modal-container,
.modal-fade-leave-to .modal-container {
  transform: scale(0.95);
}

/* --- Scrollbar --- */
.modal-container {
     overflow-y: auto;
     /* ✨ 스크롤바 숨김 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}
.notice-list::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #dee2e6;
  background-color: #f8f9fa;
  padding: 16px 32px;
  margin: -24px -32px 0; /* 부모의 패딩을 무시하고 상단에 붙임 */
}

.modal-title {
  font-size: 22px;
  font-weight: 700;
  color: #212529;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
}

.category-tabs {
  display: flex;
  justify-content: space-between;
  margin: 20px 0;
  gap: 8px;
}

.category-tabs button {
  flex: 1;
  padding: 12px;
  font-weight: bold;
  background: #e9ecef;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 14px;
  color: #495057;
}

.category-tabs button.active {
  background: #3b82f6;
  color: white;
}

.subcategory-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.subcategory-buttons button {
  background: #f8f9fa;
  border: none;
  padding: 8px 12px;
  border-radius: 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
  font-weight: 500;
  color: #495057;
}

.subcategory-buttons button.active {
  background: #495057;
  color: white;
  font-weight: 600;
}

.item-content {
  padding-top: 20px;
}

.item-content img {
  width: 70%;
  height: auto;
  margin-top: 15px;
  border-radius: 8px;
}

.item-content p {
  font-size: 16px;
  line-height: 1.5;
}
.help-image {
  width: 100%;        /* 부모 너비에 맞춤 */
  max-width: 800px;   /* 최대 너비 제한 */
  height: auto;       /* 비율 유지 */
  border-radius: 8px;
  margin: 0 auto;
  display: block;
}
.description-box {
  background-color: #ffffff;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  padding: 20px;
  margin-top: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.description-box p {
  margin: 0;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}


</style>
