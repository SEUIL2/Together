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
        <li>
  <button :class="{ active: $route.path.includes('/MyProject') || $route.path.includes('/professor/project') }" @click="goMyProject">
    내 프로젝트
  </button>
</li>
        <li>
  <button :class="{ active: $route.path.includes('/DashBoard') || $route.path.includes('/professor/dashboard') }" @click="goMyDashBoard">
    대시보드
  </button>
</li>
<li>
  <button :class="{ active: $route.path.includes('/TaskPage') || $route.path.includes('/professor/task') }" @click="goMyTask">
    작업
  </button>
</li>
<li>
  <button :class="{ active: $route.path.includes('/Scheduletest') || $route.path.includes('/professor/schedule') }" @click="goSchedule">
    일정 관리
  </button>
</li>
<li>
  <button :class="{ active: $route.path.includes('/TeamManagement') || $route.path.includes('/professor/team') }" @click="goTeam">
    팀원 관리
  </button>
</li>

<li v-if="!isProfessorReadOnly">
  <button :class="{ active: $route.path === '/MeetingPage' }" @click="goMeeting">회의</button>
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
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import NotificationPopup from '@/components/NotificationPopup.vue'
import ProfileSettingsModal from '@/components/ProfileSettingsModal.vue'

const API_URL = 'http://localhost:8081'
axios.defaults.baseURL = API_URL
axios.defaults.withCredentials = true

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const showMenu = ref(false)
const showProfileModal = ref(false)
const settingsRef = ref(null)

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
      await axios.post(
        '/auth/logout',
        null,
        { headers: { Authorization: localStorage.getItem('authHeader') } }
      )
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
    await axios.get('/auth/me', {
      headers: { Authorization: localStorage.getItem('authHeader') },
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

  // ✅ 로그인 성공 이벤트 수신 시 로그인 상태 재확인
  window.addEventListener('login-success', checkLoginStatus)
})


onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
const isProfessorReadOnly = computed(() => route.query.readonly === 'true')
const projectId = computed(() => {
  const fromParams = route.params.projectId
  const fromQuery = route.query.projectId

  if (typeof fromParams === 'string' || typeof fromParams === 'number') {
    return String(fromParams)
  }
  if (typeof fromQuery === 'string' || typeof fromQuery === 'number') {
    return String(fromQuery)
  }

  return '' // fallback: projectId 없을 경우 빈 문자열
})



console.log('params:', route.params)
console.log('query:', route.query)
console.log('projectId:', projectId.value)


const goMyProject = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/project/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/MyProject')
  }
}

const goMyDashBoard = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/dashboard/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/DashBoard')
  }
}

const goMyTask = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/task/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/TaskPage')
  }
}

const goSchedule = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/schedule/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/Scheduletest')
  }
}

const goTeam = () => {
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(`/professor/team/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
  } else {
    router.push('/TeamManagement')
  }
}

const goMeeting = () => {
  if (isProfessorReadOnly.value) {
    router.push(`/professor/meeting/${projectId.value}?readonly=true`)
  } else {
    router.push('/MeetingPage')
  }
}
const goHelp = () => {
  router.push('/HelpPage') // 도움말은 공통이라 조건 없이 이동
}


// 교수 읽기 전용 판단 + 프로젝트명 표시용

const projectTitle = computed(() => route.query.projectTitle || '')
const goBack = () => router.push('/professor/mainpage')
</script>

<style scoped>
.header-bar {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  height: 60px;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  box-shadow: 0 2px 6px rgba(0, 40, 120, 0.1);
}

.logo {
  display: flex;
  align-items: center;
}

.logo-img {
  height: 30px;
  margin-right: 0.8rem;
}

.logo-text {
  font-weight: bold;
  color: #3f8efc;
  font-size: 22px;
}

nav ul {
  display: flex;
  list-style: none;
  margin-left: -100px;
  padding: 0;
  gap: 4rem;
}

nav ul li button {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  color: #000;
  padding: 0;
  position: relative;
  height: 60px;
}

nav ul li button:hover {
  color: #666;
}

nav ul li button.active {
  color: #3f8efc;
}

nav ul li button.active::after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 130%;
  height: 5px;
  background-color: #3f8efc;
  border-radius: 5px;
}

.settings-icon {
  position: relative;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.settings-img {
  width: 24px;
  height: 24px;
}

.settings-popup {
  position: absolute;
  top: 40px;
  right: 0;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  padding: 8px 0;
  min-width: 120px;
  white-space: nowrap;
}

.popup-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  display: block;
  width: 100%;
  padding: 8px 12px;
  text-align: left;
}

.popup-btn:hover {
  color: #3f8efc;
}

.icon-button {
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  cursor: pointer;
}

.icon-button:focus {
  outline: none;
}

.readonly-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  font-size: 16px;
}

.project-title {
  font-weight: bold;
  color: #3f8efc;
}

.back-button {
  background: none;
  border: none;
  color: #3f8efc;
  font-size: 16px;
  cursor: pointer;
}

.back-button:hover {
  text-decoration: underline;
}
.readonly-project-box {
  margin-left: 0;
  margin-right: 0px;
  display: flex;
  align-items: center;
}

.project-badge {
  display: flex;
  align-items: center;
  background-color: rgb(255, 255, 255);
  border: 1px solid #cddafd;
  border-radius: 12px;
  padding: 4px 10px;
  /* box-shadow: 0 2px 6px rgba(0, 40, 120, 0.1); */
  transition: box-shadow 0.3s;
}

.project-badge:hover {
  box-shadow: 0 4px 12px rgba(0, 40, 120, 0.15);
}

.badge-left {
  display: flex;
  flex-direction: column;
  margin-right: 15px;
  margin-left: 5px;
}


.project-name {
  font-size: 16px;
  font-weight: bold;
  color: #000000;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.return-btn {
  background-color: #ffffff;
  border: 1px solid #aab6fe;
  border-radius: 8px;
  padding: 6px 10px;
  font-size: 13px;
  font-weight: 500;
  color: #3f51b5;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.return-btn:hover {
  background-color: #dbe3ff;
  border-color: #94a3ff;
}
</style>
