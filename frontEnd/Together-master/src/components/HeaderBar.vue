<template>
  <div class="header-wrapper">
    <header class="header-bar" @mouseleave="showAllSubmenus = false">
      <div class="logo" @click="goMyDashBoard">
        <img src="@/assets/togetherlogo.png" alt="Together Logo" class="logo-img" />
        <span class="logo-text">TOGETHER</span>
      </div>

      <nav v-if="!isProfessor || isProfessorReadOnly" class="main-nav" @mouseenter="showAllSubmenus = true">
        <ul>
          <li><button :class="{ active: $route.query.step === '기획' }" @click="goToStep('기획')">기획</button></li>
          <li><button :class="{ active: $route.query.step === '설계' }" @click="goToStep('설계')">설계</button></li>
          <li><button :class="{ active: $route.query.step === '개발' }" @click="goToStep('개발')">개발</button></li>
          <li><button :class="{ active: $route.query.step === '테스트' }" @click="goToStep('테스트')">테스트</button></li>
        </ul>
      </nav>

      <div class="settings-icon">
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

    <ProfileSettingsModal
        :visible="showProfileModal"
        @close="showProfileModal = false"
        @updated="onProfileUpdated"
    />

    <div v-if="!isProfessor || isProfessorReadOnly" class="mega-menu-container" :class="{ open: showAllSubmenus }" @mouseenter="showAllSubmenus = true" @mouseleave="showAllSubmenus = false">
      <div class="mega-menu-content">
        <div class="mega-menu-column">
          <button v-for="item in planningItems" :key="item.type" class="inline-item" @click="goToSubStep('기획', item.type)">
            {{ item.name }}
          </button>
        </div>
        <div class="mega-menu-column">
          <button v-for="item in designItems" :key="item.type" class="inline-item" @click="goToSubStep('설계', item.type)">
            {{ item.name }}
          </button>
        </div>
        <div class="mega-menu-column">
          <button v-for="item in developItems" :key="item.type" class="inline-item" @click="goToSubStep('개발', item.type)">
            {{ item.name }}
          </button>
        </div>
        <div class="mega-menu-column">
          <button v-for="item in testItems" :key="item.type" class="inline-item" @click="goToSubStep('테스트', item.type)">
            {{ item.name }}
          </button>
        </div>
        <div class="mega-menu-column notification-section">
          <div class="popup-header">
            <h3>알림</h3>
            <span v-if="unreadCount > 0" class="unread-dot"></span>
          </div>
          <div v-if="invitations.length === 0 && notifications.length === 0" class="no-notifications">
            <p>새로운 알림이 없습니다.</p>
          </div>
          <ul v-else class="notification-list">
            <li v-for="invite in invitations" :key="`invite-${invite.invitationId}`" class="notification-item">
              <img src="@/assets/invite.png" class="item-icon" alt="초대 아이콘"/>
              <div class="notification-content">
                <p class="message">
                  <strong>{{ invite.projectTitle }}</strong> 프로젝트에 초대되었습니다.
                </p>
                <span class="time">{{ formatTime(invite.createdAt) }}</span>
              </div>
              <div class="actions">
                <button class="accept-btn" @click.stop="accept(invite)">수락</button>
                <button class="reject-btn" @click.stop="reject(invite.invitationId)">거절</button>
              </div>
            </li>
            <li v-for="noti in notifications" :key="`noti-${noti.id}`" class="notification-item" @click="confirm(noti)">
              <img :src="getNotificationIconPath(noti)" class="item-icon" alt="알림 아이콘"/>
              <div class="notification-content">
                <p class="message">
                  {{ noti.message }}
                </p>
                <span class="time">{{ formatTime(noti.createdAt) }}</span>
              </div>
              <div class="actions">
                <button class="confirm-btn" @click.stop="confirm(noti)">확인</button>
              </div>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import ProfileSettingsModal from '@/components/ProfileSettingsModal.vue'
import noticeIcon from '@/assets/notice.png'
import voteIcon from '@/assets/vote.png'
import todoIcon from '@/assets/todo.png'
import defaultIcon from '@/assets/bell.png'

const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const userRole = ref('')
const showMenu = ref(false)
const showProfileModal = ref(false)
const settingsRef = ref(null)

const isProfessor = computed(() => userRole.value === 'PROFESSOR')
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
  if (isProfessorReadOnly.value && projectId.value) {
    router.push(
        `/professor/dashboard/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`
    )
  } else if (isProfessor.value) {
    router.push('/professor/MainPage')
  } else {
    router.push('/DashBoard')
  }
}

const showAllSubmenus = ref(false)
const planningItems = [
  { name: '프로젝트 동기', type: 'motivation' },
  { name: '프로젝트 목표', type: 'goal' },
  { name: '요구사항 정의', type: 'requirement' },
  { name: '정보구조도', type: 'infostructure' },
  { name: '스토리보드', type: 'storyboard' }
]

const designItems = [
  { name: "유스케이스 다이어그램", type: "usecase" },
  { name: "클래스 다이어그램", type: "classDiagram" },
  { name: "시퀀스 다이어그램", type: "sequence" },
  { name: "UI 디자인", type: "ui" },
  { name: "ERD", type: "erd" },
  { name: "테이블 명세", type: "table" },
  { name: "시스템 아키텍쳐", type: "architecture" },
]

const developItems = [
  { name: '개발 환경 설정', type: 'environment' },
  { name: '기능별 개발 순서', type: 'devOrder' },
  { name: '커밋 메시지 규칙', type: 'commitRule' },
  { name: '폴더 구조 및 파일 규칙', type: 'folder' }
]

const testItems = [
  { name: '단위 테스트', type: 'unit' },
  { name: '통합 테스트', type: 'integration' }
]

// --- Notification Logic Start ---
const invitations = ref([])
const notifications = ref([])
let pollingTimer = null

watch(showAllSubmenus, (newValue) => {
  if (newValue) {
    fetchAllNotifications();
  }
});

function getNotificationIconPath(notification) {
  const message = notification.message || '';
  if (message.includes('공지사항')) return noticeIcon;
  if (message.includes('투표')) return voteIcon;
  if (message.includes('작업')) return todoIcon;
  return defaultIcon;
}

async function fetchAllNotifications() {
  await fetchInvitations();
  await fetchUnreadNotifications();
}

async function fetchInvitations() {
  try {
    const resp = await axios.get('/projects/invitations', { withCredentials: true });
    invitations.value = (Array.isArray(resp.data) ? resp.data : [])
        .filter(inv => inv.status === 'PENDING')
        .map(inv => ({ ...inv, createdAt: inv.createdAt || inv.createdDate }));
  } catch (e) { console.error('초대 목록 조회 실패', e); }
}

async function fetchUnreadNotifications() {
  try {
    const resp = await axios.get('/notifications/unread', { withCredentials: true });
    notifications.value = Array.isArray(resp.data) ? resp.data : [];
  } catch (e) { console.error('일반 알림 조회 실패', e); }
}

async function accept(invitation) {
  try {
    await axios.post(`/projects/invite/${invitation.invitationId}/accept`, null, { withCredentials: true });
    fetchAllNotifications();
    const { data: me } = await axios.get('/auth/me', { withCredentials: true });
    const isProfessor = (me.roles || []).some(r => r.authority === 'ROLE_PROFESSOR');
    if (isProfessor) {
      const res = await axios.get('/projects/my-projects/sorted-by-created', { withCredentials: true });
      const matched = (res.data || []).find(p => p.title === invitation.projectTitle);
      router.push(matched ? `/professor/project/${matched.projectId}?readonly=true&projectTitle=${encodeURIComponent(matched.title)}` : '/professor/mainpage');
    } else {
      router.push('/MyProject');
    }
  } catch (e) { console.error('초대 수락 실패', e); }
}

async function reject(invitationId) {
  try {
    await axios.post(`/projects/invitations/${invitationId}/reject`, null, { withCredentials: true });
    fetchAllNotifications();
  } catch (e) { console.error('초대 거절 실패', e); }
}

async function confirm(noti) {
  try {
    await axios.post(`/notifications/${noti.id}/read`, null, { withCredentials: true });
    fetchAllNotifications();
    if (noti.linkUrl) {
      router.push(noti.linkUrl);
    }
  } catch (e) {
    console.error('알림 확인 실패', e);
  }
}

function formatTime(dateStr) {
  if (!dateStr) return '';
  const now = new Date();
  const d = new Date(dateStr);
  if (isNaN(d.getTime())) return '유효하지 않은 시간';
  const diffMs = now - d;
  const diffSecs = Math.round(diffMs / 1000);
  const diffMins = Math.round(diffSecs / 60);
  const diffHours = Math.round(diffMins / 60);
  const diffDays = Math.round(diffHours / 24);
  if (diffSecs < 60) return '방금 전';
  if (diffMins < 60) return `${diffMins}분 전`;
  if (diffHours < 24) return `${diffHours}시간 전`;
  if (diffDays === 1) return '어제';
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`;
}

const unreadCount = computed(() => invitations.value.length + notifications.value.length);
// --- Notification Logic End ---


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
    isLoggedIn.value = true
    const roles = response.data.roles || []
    const isProf = roles.some(role => role.authority === 'ROLE_PROFESSOR')
    userRole.value = isProf ? 'PROFESSOR' : 'STUDENT'
  } catch {
    isLoggedIn.value = false
    userRole.value = ''
  }
}

onMounted(() => {
  checkLoginStatus()
  document.addEventListener('click', handleClickOutside)
  window.addEventListener('login-success', checkLoginStatus)
  // Start polling for notifications
  fetchAllNotifications();
  pollingTimer = setInterval(fetchAllNotifications, 10000);
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  clearInterval(pollingTimer);
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
  if (route.path.includes('/MyProject') || route.path.includes('/professor/project')) {
    router.push({ query: { ...route.query, step: stepName } });
  } else {
    router.push({ path: '/MyProject', query: { step: stepName } });
  }
}

const goToSubStep = (step, subStepType) => {
  if (step === '개발' && subStepType === 'environment') {
    router.push('/development-environment');
    showAllSubmenus.value = false;
    return;
  }
  if (isProfessorReadOnly.value && projectId.value) {
    router.push({
      path: `/professor/project/${projectId.value}`,
      query: { ...route.query, step, substep: subStepType }
    });
  } else {
    router.push({ path: '/MyProject', query: { step, substep: subStepType } });
  }
  showAllSubmenus.value = false;
};
</script>

<style scoped>
/* CSS 변수를 컴포넌트의 최상위 래퍼에 정의하여 하위 요소들이 모두 접근할 수 있도록 합니다. */
.header-wrapper {
  --menu-start-margin: 4rem;
  --menu-gap: 3rem;
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
  width: 16%;
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
  width: 140px;
  background:none; border:none; height:60px; padding:0 2px;
  font-size:1rem; cursor:pointer; color:#000; position:relative;
  display:inline-flex; align-items:center; justify-content: center;
  transition: transform 0.2s ease, color 0.2s ease;
}
nav ul li button:hover{
  color: var(--brand);
  transform: translateY(-2px);
}
nav ul li button.active{
  color: var(--brand);
  position: relative;
}

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
  top: 60px; /* 헤더 높이 */
  left: 0;
  width: 100%;
  background: #fff;
  z-index: 1000;
  overflow: hidden;
  max-height: 0;
  transition: max-height 0.35s ease-in-out, padding 0.35s ease-in-out, border-color 0.35s ease-in-out;
  border-bottom: 1px solid transparent;
}

.mega-menu-container.open {
  max-height: 500px;
  padding: 24px 0;
  border-bottom-color: #e0e0e0;
  box-shadow: 0 8px 16px rgba(0,0,0,0.08);
}

.mega-menu-content {
  display: flex;
  gap: var(--menu-gap);
  width: 100%;
  margin: 0;
  padding-left: calc(16% + var(--menu-start-margin));
}

.mega-menu-column {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 140px;
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
  white-space: nowrap;
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


/* --- Notification Styles --- */
.notification-section {
  width: 380px;
  margin-left: auto;
  padding-right: 2rem;
}
.popup-header {
  display: flex;
  align-items: center;
  padding: 0 0 16px 0;
  border-bottom: 1px solid #f0f0f0;
}
.popup-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.unread-dot {
  margin-left: 8px;
  width: 9px;
  height: 9px;
  background-color: #ff3b30;
  border-radius: 50%;
}
.notification-list {
  padding: 8px 0;
  margin: 0;
  list-style: none;
  max-height: 350px;
  overflow-y: auto;
}
.notification-item {
  display: grid;
  grid-template-columns: 40px 1fr auto;
  grid-template-areas: "icon content actions";
  gap: 16px;
  padding: 16px 5px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
}
.notification-item:last-child {
  border-bottom: none;
}
.notification-item:hover {
  background-color: #f9f9f9;
}
.item-icon {
  grid-area: icon;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.notification-content {
  grid-area: content;
  display: flex;
  flex-direction: column;
}
.actions {
  grid-area: actions;
  display: flex;
  gap: 10px;
}
.message {
  font-size: 14px;
  color: #333;
  margin: 0 0 4px 0;
  line-height: 1.5;
}
.message strong {
  font-weight: 600;
  color: #5a76f3;
}
.time {
  font-size: 12px;
  color: #888;
}
.accept-btn, .reject-btn, .confirm-btn {
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
  white-space: nowrap;
}
.accept-btn { background-color: #5a76f3; color: white; }
.accept-btn:hover { background-color: #4862d1; }
.reject-btn { background-color: #f1f1f1; color: #555; }
.reject-btn:hover { background-color: #e0e0e0; }
.confirm-btn { background-color: #e9ecf3; color: #5a76f3; }
.confirm-btn:hover { background-color: #d8dbe5; }
.no-notifications {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

</style>