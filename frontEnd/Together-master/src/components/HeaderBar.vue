<template>
  <!-- 헤더 바 -->
  <header class="header-bar">
    <!-- 로고 영역 -->
    <div class="logo">
      <img src="@/assets/togetherlogo.png" alt="Together Logo" class="logo-img" />
      <span class="logo-text">TOGETHER</span>
    </div>

    <!-- 메뉴 영역 -->
    <nav>
      <ul>
        <!-- 대시보드 -->
        <li v-if="!isProfessorReadOnly">
          <button :class="{ active: $route.path === '/DashBoard' }" @click="goMyDashBoard">
            대시보드
          </button>
        </li>

        <!-- 내 프로젝트 -->
        <li>
          <button
            :class="{ active: $route.path.includes('/MyProject') || $route.path.includes('/professor/project') }"
            @click="goMyProject"
          >
            내 프로젝트
          </button>
        </li>

        <!-- ✅ 진행(인라인 가로 서브메뉴) -->
        <li
          class="has-inline"
          @mouseenter="progressOpen = true"
          @mouseleave="progressOpen = false"
          :class="{ open: progressOpen }"
        >
          <button
            type="button"
            class="dropdown-trigger pretty"
            :aria-expanded="progressOpen ? 'true' : 'false'"
            aria-haspopup="true"
            :class="{
              active:
                $route.path.includes('/TaskPage') ||
                $route.path.includes('/Scheduletest') ||
                $route.path.includes('/professor/task') ||
                $route.path.includes('/professor/schedule')
            }"
          >
            <!-- 밑줄/애니메이션을 텍스트에만 적용할 수 있도록 래핑 -->
            <span class="trigger-label">{{ progressLabel }}</span>
            <svg class="caret" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
              <path
                d="M7 10l5 5 5-5"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
          </button>

          <!-- 헤더 내부에 가로로 펼쳐지는 서브메뉴 -->
          <div class="inline-submenu" v-show="progressOpen">
            <button
              class="inline-item"
              :class="{ active: $route.path.includes('/TaskPage') || $route.path.includes('/professor/task') }"
              @click="goMyTask"
            >
              작업
            </button>
            <button
              class="inline-item"
              :class="{ active: $route.path.includes('/Scheduletest') || $route.path.includes('/professor/schedule') }"
              @click="goSchedule"
            >
              일정관리
            </button>
          </div>
        </li>
<!-- ✅ 보고(인라인 가로 서브메뉴) -->
<li
  class="has-inline"
  @mouseenter="reportOpen = true"
  @mouseleave="reportOpen = false"
  :class="{ open: reportOpen }"
>
  <button
    type="button"
    class="dropdown-trigger pretty"
    :aria-expanded="reportOpen ? 'true' : 'false'"
    aria-haspopup="true"
    :class="{
      active:
        $route.path.includes('/Feedback') ||
        $route.path.includes('/Report') ||
        $route.path.includes('/professor/feedback') ||
        $route.path.includes('/professor/report')
    }"
  >
    <span class="trigger-label">보고</span>
    <svg class="caret" viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <path
        d="M7 10l5 5 5-5"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
      />
    </svg>
  </button>

  <div class="inline-submenu" v-show="reportOpen">
    <button
      class="inline-item"
      :class="{ active: $route.path.includes('/Feedback') || $route.path.includes('/professor/feedback') }"
      @click="goFeedback"
    >
      피드백
    </button>
    <button
      class="inline-item"
      :class="{ active: $route.path.includes('/Report') || $route.path.includes('/professor/report') }"
      @click="goReport"
    >
      보고서
    </button>
  </div>
</li>

        <!-- 회의 -->
        <li v-if="!isProfessorReadOnly">
          <button :class="{ active: $route.path === '/MeetingPage' }" @click="goMeeting">회의</button>
        </li>

        <!-- 팀원 관리 -->
        <li>
          <button
            :class="{ active: $route.path.includes('/TeamManagement') || $route.path.includes('/professor/team') }"
            @click="goTeam"
          >
            팀원 관리
          </button>
        </li>

        <!-- 도움말: 교수 읽기 전용이면 숨김 -->
        <li v-if="!isProfessorReadOnly">
          <button :class="{ active: $route.path === '/HelpPage' }" @click="goHelp">도움말</button>
        </li>
      </ul>
    </nav>

    <!-- 읽기 전용 프로젝트 카드 + 돌아가기 버튼 -->
    <div v-if="isProfessorReadOnly" class="readonly-project-box">
      <div class="project-badge">
        <div class="badge-left">
          <span class="project-name">{{ projectTitle }}</span>
        </div>
        <button class="return-btn" @click="goBack">돌아가기</button>
      </div>
    </div>

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
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import NotificationPopup from '@/components/NotificationPopup.vue'
import ProfileSettingsModal from '@/components/ProfileSettingsModal.vue'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const showMenu = ref(false)
const showProfileModal = ref(false)
const settingsRef = ref(null)

const progressOpen = ref(false)
const progressLabel = ref('진행')

const toggleMenu = () => (showMenu.value = !showMenu.value)
const openProfile = () => {
  showProfileModal.value = true
  showMenu.value = false
}
const onProfileUpdated = () => {
  checkLoginStatus()
}

const authHeader = localStorage.getItem('authHeader')
if (authHeader) {
  axios.defaults.headers.common['Authorization'] = authHeader
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

const checkLoginStatus = async () => {
  try {
    const authHeader = localStorage.getItem('authHeader')
    if (authHeader) {
      axios.defaults.headers.common['Authorization'] = authHeader
    }
    await axios.get('/auth/me', {
      headers: { Authorization: authHeader },
      withCredentials: true,
    })
    isLoggedIn.value = true
  } catch {
    isLoggedIn.value = false
  }
}

const handleClickOutside = (e) => {
  if (showMenu.value && settingsRef.value && !settingsRef.value.contains(e.target)) {
    showMenu.value = false
  }
}

onMounted(() => {
  checkLoginStatus()
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('login-success', checkLoginStatus)
  syncProgressLabelWithRoute()
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

/* ====== 진행 라벨 경로 동기화 ====== */
function syncProgressLabelWithRoute() {
  if (route.path.includes('/TaskPage') || route.path.includes('/professor/task')) {
    progressLabel.value = '작업'
  } else if (route.path.includes('/Scheduletest') || route.path.includes('/professor/schedule')) {
    progressLabel.value = '일정관리'
  } else {
    progressLabel.value = '진행'
  }
}
watch(() => route.path, syncProgressLabelWithRoute)

/* ====== 읽기 전용/프로젝트 ====== */
const isProfessorReadOnly = computed(() => route.query.readonly === 'true')
const projectId = computed(() => {
  const fromParams = route.params.projectId
  const fromQuery = route.query.projectId
  if (typeof fromParams === 'string' || typeof fromParams === 'number') return String(fromParams)
  if (typeof fromQuery === 'string' || typeof fromQuery === 'number') return String(fromQuery)
  return ''
})
const projectTitle = computed(() => route.query.projectTitle || '')
const goBack = () => router.push('/professor/mainpage')

/* ====== 라우팅 함수 ====== */
const goMyProject = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/project/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/MyProject')
  }
  progressLabel.value = '진행'
}

const goMyDashBoard = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/dashboard/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/DashBoard')
  }
  progressLabel.value = '진행'
}

const goMyTask = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/task/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/TaskPage')
  }
  progressLabel.value = '작업'
  progressOpen.value = false
}

const goSchedule = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/schedule/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/Scheduletest')
  }
  progressLabel.value = '일정관리'
  progressOpen.value = false
}
// ▼ 진행과 동일하게 열림 상태 추가
const reportOpen = ref(false)

// ▼ 라우팅 함수 추가 (경로는 프로젝트 구조에 맞게 필요 시 수정)
const goFeedback = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/feedback/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/Feedback') // ← 너희 라우트에 맞게 '/FeedbackPage' 등으로 바꿔도 됨
  }
  reportOpen.value = false
}

const goReport = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/report/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/Report') // ← 필요 시 '/ReportPage' 등으로 변경
  }
  reportOpen.value = false
}

const goTeam = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/team/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/TeamManagement')
  }
  progressLabel.value = '진행'
}

const goMeeting = () => {
  if (isProfessorReadOnly.value) {
    router.push(`/professor/meeting/${projectId.value}?readonly=true`)
  } else {
    router.push('/MeetingPage')
  }
  progressLabel.value = '진행'
}

const goHelp = () => {
  router.push('/HelpPage')
  progressLabel.value = '진행'
}
</script>


<style scoped>
/* ===== 토큰 ===== */
:root{
  --brand:#3f8efc;
  --text:#0f172a;
  --line:#eee;
  --shadow:0 2px 6px rgba(0,40,120,.08);
}

/* ===== 헤더 바 기본 ===== */
.header-bar{
  position:fixed; top:0; left:0; width:100%; height:60px; z-index:1000;
  display:flex; align-items:center; justify-content:space-between;
  padding:0 2rem; background:#fff; border-bottom:1px solid var(--line); box-shadow:var(--shadow);
  overflow:visible;
}
.logo{display:flex; align-items:center}
.logo-img{height:30px; margin-right:.8rem}
.logo-text{font-weight:700; color:var(--brand); font-size:22px}

/* ===== 내비게이션 기본 ===== */
nav ul{display:flex; align-items:center; gap:3rem; list-style:none; margin:0; padding:0}
nav ul li{position:relative}
nav ul li button{
  background:none; border:none; height:60px; padding:0 2px;
  font-size:1rem; cursor:pointer; color:#000; position:relative;
  display:inline-flex; align-items:center; gap:6px;
}
nav ul li button:hover{color:#666}
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


/* ===== 진행(인라인 서브메뉴) ===== */
.has-inline{position:relative}

/* 트리거: ‘진행’ */
.dropdown-trigger.pretty{
  background:none; border:0; height:60px; padding:0 4px;
  font-size:1rem; color:var(--text); cursor:pointer; display:inline-flex; align-items:center; gap:6px;
}


/* 텍스트만 밑줄 & 애니메이션 대상 */
.trigger-label{
  position:relative; display:inline-block; padding-bottom:2px;
  transform-origin:50% 100%;
  transition: transform .18s ease, color .18s ease;
}
/* 활성 시 텍스트 밑줄 (아이콘 제외) */
.dropdown-trigger.pretty.active .trigger-label::after{
  content:""; position:absolute; left:0; right:0; bottom:0; height:3px;
  background:var(--brand); border-radius:2px;
}

/* 캐럿(▼): 텍스트와 함께 축소/상승 + 회전 */
.caret{
  width:1em; height:1em;         /* 폰트 크기에 비례하도록 */
  transition: transform .18s ease, opacity .18s ease;
  opacity:.75;
}
.has-inline.open .caret{
  transform: translateY(-8px) scale(.88) rotate(180deg);
  opacity:1;
}

/* 마우스 오버 시 ‘진행’ 텍스트 위로/축소 */
.has-inline.open .trigger-label{
  transform: translateY(-8px) scale(.88);
  color:var(--brand);
}

/* 가로 서브메뉴(헤더 내부 전개) */
.inline-submenu{
  position:absolute; 
  left:50%; 
  transform:translateX(-50%);
  top:36px; /* 진행 텍스트 아래 라인 — 폰트에 맞게 34~38px로 조절 가능 */
  display:flex; 
  align-items:center; 
  gap:0px;
  background:#fff; 
  z-index:1100;
  height: 45px;
  /* 추가된 디자인 */
  border:1px solid #e5e7eb;   /* 연한 테두리 */
  border-radius:8px;          /* 모서리 둥글게 */
padding:14px 10px; /* 위아래 14px, 좌우 20px */
  opacity:0; 
  pointer-events:none; 
  transition:opacity .15s ease, transform .15s ease;
}

.has-inline.open .inline-submenu{
  opacity:1; 
  pointer-events:auto; 
  transform:translateX(-50%);
}


/* 서브 아이템: 중앙 정렬 + 스프링 같은 작은 모션 + 밑줄 애니메이션 */
.inline-item{
  background:none; border:0; padding:6px 6px; min-width:64px;
  font-size:14px; color:var(--text); cursor:pointer; position:relative; text-align:center;
  transition: color .15s ease, transform .12s cubic-bezier(.2,.9,.3,1.2);
}
.inline-item:hover{
  color:var(--brand);
  transform: translateY(-1px);
}
/* 활성/호버 밑줄: 텍스트 중심 기준으로 부드럽게 */
.inline-item::after{
  content:""; position:absolute; left:50%; bottom:-4px; height:3px; width:0;
  background:var(--brand); border-radius:2px; transform:translateX(-50%);
  transition: width .16s ease;
}
.inline-item.active::after,
.inline-item:hover::after{ width:100%; }
.dropdown-trigger.pretty.active {
  color: #3f8efc;
}

.dropdown-trigger.pretty.active .trigger-label::after {
  content: "";
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 3px;
  background: #3f8efc;
  border-radius: 2px;
}

/* ===== 설정/뱃지 등 간단 유지 ===== */
.settings-icon{position:relative; display:flex; align-items:center; gap:1rem}
.settings-img{width:24px; height:24px}
.settings-popup{
  position:absolute; top:40px; right:0; background:#fff; border:1px solid #ddd; border-radius:6px;
  box-shadow:0 4px 6px rgba(0,0,0,.1); padding:8px 0; min-width:120px; white-space:nowrap;
}
.popup-btn{background:none; border:none; width:100%; text-align:left; padding:8px 12px; font-size:14px; color:#333; cursor:pointer}
.popup-btn:hover{color:var(--brand)}
.icon-button{background:none; border:none; cursor:pointer}
.icon-button:focus{outline:none}

.readonly-project-box{display:flex; align-items:center}
.project-badge{display:flex; align-items:center; background:#fff; border:1px solid #cddafd; border-radius:12px; padding:4px 10px; transition:.2s}
.project-badge:hover{box-shadow:0 4px 12px rgba(0,40,120,.15)}
.badge-left{display:flex; flex-direction:column; margin:0 15px 0 5px}
.project-name{font-size:16px; font-weight:700; color:#000; max-width:180px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis}
.return-btn{background:#fff; border:1px solid #aab6fe; border-radius:8px; padding:6px 10px; font-size:13px; color:#3f51b5; cursor:pointer}
.return-btn:hover{background:#dbe3ff; border-color:#94a3ff}
.header-bar::after{
  content:"";
  position:absolute;
  left:0; right:0; bottom:0;
  height:1px;
  background:#ddd; /* 원하는 선 색상 */
  z-index:0;    /* 메뉴보다 위에 오도록 */
}

</style>
