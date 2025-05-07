<template>
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
          <button 
            :class="{ active: $route.path === '/MyProject' }" 
            @click="goMyProject"
          >
            내 프로젝트
          </button>
        </li>
        <li>
          <button 
            :class="{ active: $route.path === '/DashBoard' }"
            @click="goMyDashBoard"
          >
            대시보드
          </button>
        </li>
        <li><button @click="goMyTask">작업</button></li>
        <li><button @click="goSchedule">일정 관리</button></li>
        <li>
          <button 
            :class="{ active: $route.path === '/TeamManagement' }"
            @click="goTeam"
          >
            팀원 관리
          </button>
        </li>
        <li>
          <button 
            :class="{ active: $route.path === '/MeetingPage' }"
            @click="goMeeting"
          >
            회의
          </button>
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
          <button class="popup-btn" @click="handleAuth">
            {{ isLoggedIn ? '로그아웃' : '로그인' }}
          </button>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import NotificationPopup from '@/components/NotificationPopup.vue'

const router = useRouter()

// 페이지 이동 함수
const goMyProject = () => router.push('/MyProject')
const goMyDashBoard = () => router.push('/DashBoard')
const goSchedule = () => router.push('/Scheduletest')
const goMeeting = () => router.push('/MeetingPage')
const goTeam = () => router.push('/TeamManagement')
const goMyTask = () => router.push('/TaskPage')

// 로그인 상태 & 설정 팝업 표시 여부
const isLoggedIn = ref(false)
const showMenu = ref(false)

// 팝업 DOM 참조
const settingsRef = ref(null)

// 설정 팝업 토글
function toggleMenu() {
  showMenu.value = !showMenu.value
}

// 외부 클릭 시 설정 팝업 닫기
function handleClickOutside(event) {
  if (
    showMenu.value &&
    settingsRef.value &&
    !settingsRef.value.contains(event.target)
  ) {
    showMenu.value = false
  }
}

// 로그인/로그아웃 버튼 처리
async function handleAuth() {
  if (isLoggedIn.value) {
    try {
      await axios.post('/auth/logout', null, { withCredentials: true })
      localStorage.removeItem('authHeader')
      isLoggedIn.value = false
      alert('로그아웃 되었습니다.')
      window.location.href = '/'
    } catch (e) {
      alert('로그아웃 중 오류가 발생했습니다.')
    }
  } else {
    router.push('/login')
  }
  showMenu.value = false
}

// 로그인 상태 확인
const checkLoginStatus = async () => {
  try {
    const response = await axios.get('/auth/me', {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
      },
      withCredentials: true,
    })
    if (response.status === 200) {
      isLoggedIn.value = true
    }
  } catch (err) {
    isLoggedIn.value = false
  }
}

// 마운트 시 실행
onMounted(() => {
  checkLoginStatus()
  window.addEventListener('login-success', checkLoginStatus)
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
  background-color: #ffffff;
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
  margin-left: -20rem;
}

nav ul li button {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  color: #000;
  padding: 0;
  transition: color 0.3s;
  position: relative;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
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
  justify-content: center;
}

.settings-icon button {
  background: none;
  border: none;
  padding: 0;
  cursor: pointer;
}

.settings-img {
  width: 24px;
  height: 24px;
  object-fit: contain;
}

.settings-popup {
  position: absolute;
  top: 40px;
  right: 0;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  padding: 8px 12px;
  z-index: 2000;
}

.popup-btn {
  background: none;
  border: none;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  white-space: nowrap;
}

.popup-btn:hover {
  color: #3f8efc;
}
</style>
