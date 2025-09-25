<template>
  <div class="header-wrapper">
    <!-- 헤더 바 -->
    <header class="header-bar" @mouseleave="showAllSubmenus = false">
      <!-- 로고 영역 -->
      <div class="logo" @click="goMyDashBoard">
        <img src="@/assets/togetherlogo.png" alt="Together Logo" class="logo-img" />
        <span class="logo-text">TOGETHER</span>
      </div>

      <!-- 메뉴 영역: 여기에 마우스를 올리면 드롭다운 표시 -->
      <nav v-if="!isProfessor || isProfessorReadOnly" class="main-nav" @mouseenter="showAllSubmenus = true">
        <ul>
          <li><button :class="{ active: $route.query.step === '기획' }" @click="goToStep('기획')">기획</button></li>
          <li><button :class="{ active: $route.query.step === '설계' }" @click="goToStep('설계')">설계</button></li>
          <li><button :class="{ active: $route.query.step === '개발' }" @click="goToStep('개발')">개발</button></li>
          <li><button :class="{ active: $route.query.step === '테스트' }" @click="goToStep('테스트')">테스트</button></li>
        </ul>
      </nav>

      <!-- 알림 + 설정 아이콘 영역 -->
      <div class="settings-icon">
        <NotificationPopup />
        <div ref="settingsRef">
          <button class="icon-button" @click="toggleMenu">
            <img src="@/assets/settings.png" alt="Settings" class="settings-img" />
          </button>
          <div v-if="showMenu" class="settings-popup">
            <button class="popup-btn" @click="openProfile">프로필</button>
            <button class="popup-btn" @click="handleAuth">
              {{ isLoggedIn ? '로그아웃' : '로그인' }}
            </button>
          </div>
        </div>
      </div>
    </header>

    <!-- 프로필 설정 모달 -->
    <ProfileSettingsModal
      :visible="showProfileModal"
      @close="showProfileModal = false"
      @updated="onProfileUpdated"
    />

    <!-- 메가 메뉴 드롭다운 -->
    <div v-if="!isProfessor || isProfessorReadOnly" class="mega-menu-container" :class="{ open: showAllSubmenus }" @mouseenter="showAllSubmenus = true" @mouseleave="showAllSubmenus = false">
      <div class="mega-menu-content">
        <!-- 기획 컬럼 -->
        <div class="mega-menu-column">
          <button v-for="item in planningItems" :key="item.type" class="inline-item" @click="goToSubStep('기획', item.type)">
            {{ item.name }}
          </button>
        </div>
        <!-- 설계 컬럼 -->
        <div class="mega-menu-column">
          <button v-for="item in designItems" :key="item.type" class="inline-item" @click="goToSubStep('설계', item.type)">
            {{ item.name }}
          </button>
        </div>
        <!-- 개발 컬럼 -->
        <div class="mega-menu-column">
          <button v-for="item in developItems" :key="item.type" class="inline-item" @click="goToSubStep('개발', item.type)">
            {{ item.name }}
          </button>
        </div>
        <!-- 테스트 컬럼 -->
        <div class="mega-menu-column">
          <button v-for="item in testItems" :key="item.type" class="inline-item" @click="goToSubStep('테스트', item.type)">
            {{ item.name }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import NotificationPopup from '@/components/NotificationPopup.vue'
import ProfileSettingsModal from '@/components/ProfileSettingsModal.vue'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const userRole = ref('');
const showMenu = ref(false)
const showProfileModal = ref(false)
const settingsRef = ref(null)

const isProfessor = computed(() => userRole.value === 'PROFESSOR');

const isProfessorReadOnly = computed(() => route.query.readonly === 'true')
const projectId = computed(() => {
  const fromParams = route.params.projectId
  const fromQuery = route.query.projectId
  if (typeof fromParams === 'string' || typeof fromParams === 'number') return String(fromParams)
  if (typeof fromQuery === 'string' || typeof fromQuery === 'number') return String(fromQuery)
  return ''
})
const projectTitle = computed(() => route.query.projectTitle || '')

const goMyDashBoard = () => {
  // 교수 읽기 전용 모드와 학생 모드를 구분하여 대시보드로 이동
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(
      `/professor/dashboard/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`
    )
  } else if (isProfessor.value) {
    router.push('/professor/MainPage');
  } else {
    router.push('/DashBoard')
  }
}

const showAllSubmenus = ref(false);
const planningItems = [
  { name: '프로젝트 동기', type: 'motivation' },
  { name: '프로젝트 목표', type: 'goal' },
  { name: '요구사항 정의', type: 'requirement' },
  { name: '정보구조도', type: 'infostructure' },
  { name: '스토리보드', type: 'storyboard' }
];

const designItems = [
  { name: "유스케이스 다이어그램", type: "usecase" },
  { name: "클래스 다이어그램", type: "classDiagram" },
  { name: "시퀀스 다이어그램", type: "sequence" },
  { name: "UI 디자인", type: "ui" },
  { name: "ERD", type: "erd" },
  { name: "테이블 명세", type: "table" },
  { name: "시스템 아키텍쳐", type: "architecture" },
];

const developItems = [
    { name: '개발 환경 설정', type: 'environment' },
    { name: '기능별 개발 순서', type: 'devOrder' },
    { name: '커밋 메시지 규칙', type: 'commitRule' },
    { name: '폴더 구조 및 파일 규칙', type: 'folder' }
];

const testItems = [
    { name: '단위 테스트', type: 'unit' },
    { name: '통합 테스트', type: 'integration' }
];

const authHeader = localStorage.getItem('authHeader')
if (authHeader) {
  axios.defaults.headers.common['Authorization'] = authHeader
}

const checkLoginStatus = async () => {
  try {
    const authHeader = localStorage.getItem('authHeader')
    if (authHeader) {
      axios.defaults.headers.common['Authorization'] = authHeader
    }
    const response = await axios.get('/auth/me', {
      headers: { Authorization: authHeader },
      withCredentials: true,
    })
    isLoggedIn.value = true;
    const roles = response.data.roles || [];
    const isProf = roles.some(role => role.authority === 'ROLE_PROFESSOR');
    userRole.value = isProf ? 'PROFESSOR' : 'STUDENT';
  } catch {
    isLoggedIn.value = false;
    userRole.value = '';
  }
}

onMounted(() => {
  checkLoginStatus()
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('login-success', checkLoginStatus)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

const toggleMenu = () => (showMenu.value = !showMenu.value)

const openProfile = () => {
  showProfileModal.value = true
  showMenu.value = false
}

const onProfileUpdated = () => {
  checkLoginStatus()
}

const handleAuth = async () => {
  showMenu.value = false
  if (isLoggedIn.value) {
    try {
      await axios.post('/auth/logout', null, {
        headers: { Authorization: localStorage.getItem('authHeader') },
      })
      localStorage.removeItem('authHeader')
      delete axios.defaults.headers.common['Authorization']
      isLoggedIn.value = false
      alert('로그아웃 되었습니다.')
      window.location.href = '/'
    } catch (e) {
      console.error(e)
      alert('로그아웃 중 오류가 발생했습니다.')
    }
  } else {
    router.push('/login')
  }
}

const handleClickOutside = (e) => {
  if (showMenu.value && settingsRef.value && !settingsRef.value.contains(e.target)) {
    showMenu.value = false
  }
}

const goToStep = (stepName) => {
  // 현재 라우트가 MyProject 관련 페이지일 경우, 쿼리만 변경
  if (route.path.includes('/MyProject') || route.path.includes('/professor/project')) {
    router.push({ query: { ...route.query, step: stepName } });
  } else {
    // 다른 페이지에 있을 경우, MyProject 페이지로 이동하면서 쿼리 추가
    router.push({ path: '/MyProject', query: { step: stepName } });
  }
}

const goToSubStep = (step, subStepType) => {
  // 교수의 읽기 전용 모드일 경우, 현재 경로와 쿼리를 유지하며 이동
  if (isProfessorReadOnly.value && projectId.value) {
    router.push({
      path: `/professor/project/${projectId.value}`,
      query: { ...route.query, step, substep: subStepType }
    });
  } else {
    router.push({ path: '/MyProject', query: { step, substep: subStepType } });
  }
  // 클릭 후 모든 드롭다운 메뉴 닫기
  showAllSubmenus.value = false;
};
</script>


<style scoped>
/* CSS 변수를 컴포넌트의 최상위 래퍼에 정의하여 하위 요소들이 모두 접근할 수 있도록 합니다. */
.header-wrapper {
  --menu-start-margin: 4rem; /* 로고와 메뉴 시작점 사이의 간격 */
  --menu-gap: 3rem;          /* 메뉴 아이템들 사이의 간격 */
}
/* ===== 헤더 바 기본 ===== */
.header-bar{
  --brand:#3f8efc;
  --text:#0f172a;
  --line:#eee;
  position:fixed; top:0; left:0; width:100%; height:60px; z-index:1001;
  display:flex; align-items:center;
  background:#fff; border-bottom:1px solid var(--line); box-shadow:var(--shadow);
  overflow:visible;
}
.logo{
  padding-left: 24px;
  display:flex; align-items:center;
  width: 16%; /* 사이드바 너비와 동일하게 설정 */
  box-sizing: border-box;
  cursor: pointer;
}
.logo-img{height:30px; margin-right:.8rem}
.logo-text{font-weight:700; color:var(--brand); font-size:22px}
.main-nav {
  margin-left: var(--menu-start-margin);
}

/* ===== 신규 내비게이션 ===== */
nav ul{display:flex; align-items:center; gap:var(--menu-gap); list-style:none; margin:0; padding:0}
nav ul li{position:relative}
nav ul li button{
  width: 140px; /* 고정 너비로 대칭 맞춤 */
  background:none; border:none; height:60px; padding:0 2px;
  font-size:1rem; cursor:pointer; color:#000; position:relative;
  display:inline-flex; align-items:center; justify-content: center;
  transition: transform 0.2s ease, color 0.2s ease; /* 애니메이션 효과 추가 */
}
nav ul li button:hover{
  color: var(--brand); /* 호버 시 브랜드 색상으로 변경 */
  transform: translateY(-2px); /* 위로 살짝 이동하는 애니메이션 */
}
/* 공통 버튼 활성화 스타일 */
nav ul li button.active{
  color: var(--brand);
  position: relative;
}

/* 밑줄 표시 */
nav ul li button.active::after{
  content:"";
  position:absolute;
  left:0; right:0; bottom:0;
  height:3px;
  background:var(--brand);
  border-radius:2px;
}

/* ===== 메가 메뉴 드롭다운 스타일 ===== */
.mega-menu-container {
  position: absolute;
  top: 50px; /* 헤더 높이 */
  left: 0;
  width: 100%;
  background: #fff;
  z-index: 1000;
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.35s ease-in-out, padding 0.35s ease-in-out, border-color 0.35s ease-in-out;
  border-bottom: 1px solid transparent; /* 닫혔을 때 테두리 안보이게 */
}

.mega-menu-container.open {
  max-height: 400px; /* 충분한 높이 */
  padding: 24px 0;
  border-bottom-color: #e0e0e0; /* 열렸을 때 테두리 색 */
  box-shadow: 0 8px 16px rgba(0,0,0,0.08);
}

.mega-menu-content {
  display: flex;
  gap: var(--menu-gap); /* 헤더 메뉴 간격과 동일하게 설정 */
  width: 100%;
  margin: 0; /* 중앙 정렬 제거 */
  padding-left: calc(16% + var(--menu-start-margin)); /* 로고 너비 + 메뉴 시작 위치와 동일하게 설정 */
}

.mega-menu-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 140px; /* 헤더 메뉴 너비와 동일하게 설정 */
}

.inline-item {
  width: 100%;
  text-align: center;
  padding: 6px 10px;
  border-radius: 6px;
  background: none;
  border: none;
  font-size: 0.9rem;
  color: #555;
  cursor: pointer;
  white-space: nowrap; /* 텍스트 줄바꿈 방지 */
  transition: background-color 0.2s, color 0.2s;
}
.inline-item:hover {
  background-color: #f0f5ff;
  color: var(--brand);
}

/* ===== 설정/뱃지 등 간단 유지 ===== */
.settings-icon{
  position:relative; display:flex; align-items:center; gap:1rem;
  padding-right: 24px;
  margin-left: auto;
}
.settings-img{width:24px; height:24px}
.settings-popup{
  position:absolute; top:40px; right:0; background:#fff; border:1px solid #ddd; border-radius:6px;
  box-shadow:0 4px 6px rgba(0,0,0,.1); padding:8px 0; min-width:120px;
}
.popup-btn{background:none; border:none; width:100%; text-align:left; padding:8px 12px; font-size:14px; color:#333; cursor:pointer}
.popup-btn:hover{color:var(--brand)}
.icon-button{background:none; border:none; cursor:pointer}
.icon-button:focus{outline:none}

</style>
