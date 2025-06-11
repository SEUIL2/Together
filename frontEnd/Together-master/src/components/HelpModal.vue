<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-container">
      <div class="modal-header">
        <h2>📘 예시 가이드</h2>
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
      description: "데이터베이스 테이블 간의 관계를 정의합니다.",
      image: "https://via.placeholder.com/400x200?text=ERD"
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
      description: "화면 흐름 및 UI 구성을 나타냅니다.",
      image: "https://via.placeholder.com/400x200?text=화면설계"
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
        {
      title: "프로젝트 일정 계획",
      description: "화면 흐름 및 UI 구성을 나타냅니다.",
      image: "https://via.placeholder.com/400x200?text=화면설계"
    },
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
      title: "버전 관리 전략",
      description: `
      버전 관리 전략은 팀 규모와 배포 빈도에 맞춰 일관된 브랜치 운영, 커밋·태그 규칙, 머지 프로세스를 정의해 충돌을 최소화하고 안정적인 배포를 돕습니다. 
      대표적인 요소를 요약하면 다음과 같습니다.

      1. 브랜치 모델
        - 메인 브랜치(main or master)
            | 항상 배포 가능한 상태를 유지
        - 개발 브랜치(develop)
            | 다음 릴리즈를 위한 통합 브랜치
        - 기능 브랜치(feature/…)
            | 신규 기능 단위로 분기, 개발 완료 후 develop으로 PR
        - 릴리즈 브랜치(release/…)
            | QA·버그 수정 후 메인으로 머지하여 태그 생성
        - 핫픽스 브랜치(hotfix/…)
            | 운영 중 긴급 버그 수정용, 수정 후 main·develop에 머지

      2. 브랜치 네이밍 컨벤션
        - feature/login, bugfix/null-pointer 등
        - 소문자·슬래시(/) 사용, 목적·이슈 번호 포함 권장 (feature/123-login-page)

      3. 커밋 메시지 가이드
        - 형식: <type>(<scope>): <subject>
            | type: feat, fix, docs, style, refactor, test, chore
            | scope: 변경 대상 모듈명 (선택)
            | subject: 간결한 설명
        - 예) feat(auth): 로그인 세션 자동 갱신 기능 추가

      4. Pull Request & 코드 리뷰
        - PR 기준: 기능 단위·이슈 단위로 작게 쪼개기
        - 리뷰 체크리스트:
            | 동작 검증(수동/자동테스트)
            | 코드 스타일·컨벤션 준수
            | 보안·예외 처리 점검
        - 머지 방식:
            | merge commit vs squash merge 선택
            | 배포 히스토리 관리에 맞춰 통일

      5. 버전 태그 및 릴리즈
        - Semantic Versioning (vMAJOR.MINOR.PATCH)
            | MAJOR: 호환성 깨는 변경
            | MINOR: 기능 추가(하위 호환 유지)
            | PATCH: 버그 수정
        - 릴리즈 브랜치 머지 시 git tag -a v1.2.0 -m "Release v1.2.0"

      6. CI/CD 연동
        - 커밋 혹은 PR 머지 시 자동 빌드·테스트
        - 태그 시점에 자동 배포 파이프라인 트리거

      7. 권한·보호 설정
        - main/develop 브랜치 보호(Force-push 금지)
        - 리뷰 승인 1~2회 이상, 빌드 통과 시에만 머지

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
      title: "버전 관리 전략",
      description: "기능별 테스트 계획과 체크리스트를 작성합니다.",
      image: "https://via.placeholder.com/400x200?text=테스트"
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
  background: white;
  border-radius: 12px;
  width: 80%;
  height: 90%;
  overflow-y: auto;
  padding: 20px 30px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  position: relative;
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
    margin-bottom: -20px;
    margin-top: -20px;
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
  margin: 10px 0 10px;
}

.category-tabs button {
  flex: 1;
  margin: 0 5px;
  padding: 10px;
  font-weight: bold;
  background: #f0f0f0;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
    font-size: 12px;
}

.category-tabs button.active {
  background: #007bff;
  color: white;
}

.subcategory-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 20px;
}

.subcategory-buttons button {
  background: #e9ecef;
  border: none;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: 0.2s;
  font-size: 11px;
}

.subcategory-buttons button.active {
  background: #17a2b8;
  color: white;
}

.item-content {
  border-top: 1px solid #ddd;
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
  background-color: #f9f9f9;
  border: 1px solid #e0e0e0;
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
