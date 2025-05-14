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
          <button :class="{ active: $route.path === '/MyProject' }" @click="goMyProject">내 프로젝트</button>
        </li>
        <li>
          <button :class="{ active: $route.path === '/DashBoard' }" @click="goMyDashBoard">대시보드</button>
        </li>
        <li>
          <button :class="{ active: $route.path === '/TaskPage' }" @click="goMyTask">작업</button>
        </li>
        <li>
          <button :class="{ active: $route.path === '/Scheduletest' }" @click="goSchedule">일정 관리</button>
        </li>
        <li>
          <button :class="{ active: $route.path === '/TeamManagement' }" @click="goTeam">팀원 관리</button>
        </li>
        <li>
          <button :class="{ active: $route.path === '/MeetingPage' }" @click="goMeeting">회의</button>
        </li>
        <li>
          <button :class="{ active: $route.path === '/Help' }" @click="goHelp">도움말</button>
        </li>
      </ul>
    </nav>

    <!-- 알림 + 설정 아이콘 영역 -->
    <div class="settings-icon">
      <!-- 🔔 알림 팝업 (내부에서 상태 관리) -->
      <NotificationPopup />

      <!-- ⚙️ 설정 아이콘 -->
      <div ref="settingsRef">
        <button @click="toggleMenu">
          <img src="@/assets/settings.png" alt="Settings" class="settings-img" />
        </button>

        <!-- 설정 팝업 메뉴 -->
        <div v-if="showMenu" class="settings-popup">
          <!-- 프로필 메뉴 -->
          <button class="popup-btn" @click="openProfile">프로필</button>
          <!-- 로그아웃/로그인 메뉴 -->
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
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import NotificationPopup from '@/components/NotificationPopup.vue'
import ProfileSettingsModal from '@/components/ProfileSettingsModal.vue'

const API_URL = 'http://localhost:8081'
axios.defaults.baseURL = API_URL
axios.defaults.withCredentials = true

const router = useRouter()

// 페이지 이동 함수
const goMyProject = () => router.push('/MyProject')
const goMyDashBoard = () => router.push('/DashBoard')
const goMyTask = () => router.push('/TaskPage')
const goSchedule = () => router.push('/Scheduletest')
const goTeam = () => router.push('/TeamManagement')
const goMeeting = () => router.push('/MeetingPage')
const goHelp = () => router.push('/Help')

// 로그인 상태, 팝업 표시
const isLoggedIn = ref(false)
const showMenu = ref(false)
const showProfileModal = ref(false)
const settingsRef = ref(null)

// 토글
const toggleMenu = () => showMenu.value = !showMenu.value
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

// 로그인/로그아웃 핸들러
const handleAuth = async () => {
  showMenu.value = false
  if (isLoggedIn.value) {
    try {
      // (2) 전역 세팅이 아니라면, 여기에 직접 헤더 추가
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



// 로그인 상태 확인
const checkLoginStatus = async () => {
  try {
    await axios.get('/auth/me', {
      headers: {Authorization: localStorage.getItem('authHeader')},
      withCredentials: true,
    })
    isLoggedIn.value = true
  } catch {
    isLoggedIn.value = false
  }
}

// 외부 클릭 시 팝업 닫기
const handleClickOutside = (e) => {
  if (showMenu.value && settingsRef.value && !settingsRef.value.contains(e.target)) {
    showMenu.value = false
  }
}

onMounted(() => {
  checkLoginStatus()
  document.addEventListener('click', handleClickOutside)
})
onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
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
  margin: 0;
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
  padding: 8px 12px;
}

.popup-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  display: block;
  width: 100%;
  text-align: left;
}

.popup-btn:hover {
  color: #3f8efc;
}
</style>
