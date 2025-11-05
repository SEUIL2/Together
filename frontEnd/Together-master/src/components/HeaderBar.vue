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
        <!-- 알림 아이콘 버튼 -->
        <div ref="notificationRef" class="notification-button-wrapper">
          <button class="icon-button notification-btn" @click="toggleNotificationModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
          </button>
        </div>
        
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

    <!-- 알림 전용 모달 -->
    <div v-if="showNotificationModal" class="notification-modal-overlay">
      <div class="notification-modal" @click.stop>
        <div class="notification-modal-header">
          <h3>알림</h3>
          <button class="close-modal-btn" @click="closeNotificationModal">&times;</button>
        </div>
        <div class="notification-modal-content">
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
        <!-- 알림 섹션을 메가메뉴 우측에 추가 -->
        <div class="mega-menu-column notification-mega-section">
          <!-- 알림 헤더 -->
          <div class="notification-mega-header">
            <div v-if="unreadCount > 0" class="notification-text-highlight">
              새 알림이 {{ unreadCount }}개 있습니다.
            </div>
            <button class="view-all-notifications-btn" @click="toggleNotificationModal">
              모든 알림 보기
            </button>
          </div>
          
          <!-- 알림 미리보기 목록 -->
          <div class="notification-preview-list">
            <div v-if="invitations.length === 0 && notifications.length === 0" class="no-notifications-preview">
              <p>새로운 알림이 없습니다.</p>
            </div>
            <template v-else>
              <!-- 초대 알림 미리보기 (최대 2개) -->
              <div v-for="invite in invitations.slice(0, 2)" :key="`preview-invite-${invite.invitationId}`" class="notification-preview-item">
                <img src="@/assets/invite.png" class="preview-icon" alt="초대"/>
                <div class="preview-content">
                  <p class="preview-message">
                    <strong>{{ truncateText(invite.projectTitle, 15) }}</strong> 프로젝트 초대
                  </p>
                  <span class="preview-time">{{ formatTime(invite.createdAt) }}</span>
                </div>
              </div>
              
              <!-- 일반 알림 미리보기 (최대 3개, 초대 알림이 있으면 조정) -->
              <div v-for="noti in notifications.slice(0, Math.max(1, 3 - invitations.length))" :key="`preview-noti-${noti.id}`" class="notification-preview-item">
                <img :src="getNotificationIconPath(noti)" class="preview-icon" alt="알림"/>
                <div class="preview-content">
                  <p class="preview-message">{{ truncateText(noti.message, 25) }}</p>
                  <span class="preview-time">{{ formatTime(noti.createdAt) }}</span>
                </div>
              </div>
              
              <!-- 더 많은 알림이 있을 때 표시 -->
              <div v-if="(invitations.length + notifications.length) > 3" class="more-notifications-hint">
                ... 그 외 {{ (invitations.length + notifications.length) - 3 }}개의 알림
              </div>
            </template>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/api'
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
const showNotificationModal = ref(false)
const settingsRef = ref(null)
const notificationRef = ref(null)

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
  { name: "ERD", type: "erd" },
  { name: "클래스 다이어그램", type: "classDiagram" },
  { name: "시퀀스 다이어그램", type: "sequence" },
  { name: "UI 디자인", type: "ui" },
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

const toggleNotificationModal = () => {
  showNotificationModal.value = !showNotificationModal.value
  if (showNotificationModal.value) {
    fetchAllNotifications()
  }
}

const closeNotificationModal = () => {
  showNotificationModal.value = false
}

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
    const resp = await api.get('/projects/invitations', { withCredentials: true });
    invitations.value = (Array.isArray(resp.data) ? resp.data : [])
        .filter(inv => inv.status === 'PENDING')
        .map(inv => ({ ...inv, createdAt: inv.createdAt || inv.createdDate }));
  } catch (e) { console.error('초대 목록 조회 실패', e); }
}

async function fetchUnreadNotifications() {
  try {
    const resp = await api.get('/notifications/unread', { withCredentials: true });
    notifications.value = Array.isArray(resp.data) ? resp.data : [];
  } catch (e) { console.error('일반 알림 조회 실패', e); }
}

async function accept(invitation) {
  try {
    await api.post(`/projects/invite/${invitation.invitationId}/accept`, null, { withCredentials: true });
    fetchAllNotifications();
    const { data: me } = await api.get('/auth/me', { withCredentials: true });
    const isProfessor = (me.roles || []).some(r => r.authority === 'ROLE_PROFESSOR');
    if (isProfessor) {
      const res = await api.get('/projects/my-projects/sorted-by-created', { withCredentials: true });
      const matched = (res.data || []).find(p => p.title === invitation.projectTitle);
      router.push(matched ? `/professor/project/${matched.projectId}?readonly=true&projectTitle=${encodeURIComponent(matched.title)}` : '/professor/mainpage');
    } else {
      router.push('/MyProject');
    }
  } catch (e) { console.error('초대 수락 실패', e); }
}

async function reject(invitationId) {
  try {
    await api.post(`/projects/invitations/${invitationId}/reject`, null, { withCredentials: true });
    fetchAllNotifications();
  } catch (e) { console.error('초대 거절 실패', e); }
}

async function confirm(noti) {
  try {
    await api.post(`/notifications/${noti.id}/read`, null, { withCredentials: true });
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

// 텍스트 잘라내기 함수 (미리보기용)
const truncateText = (text, maxLength) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};

// --- Notification Logic End ---


const authHeader = localStorage.getItem('authHeader')
if (authHeader) {
  api.defaults.headers.common['Authorization'] = authHeader
}

const checkLoginStatus = async () => {
  try {
    const authHeader = localStorage.getItem('authHeader')
    if (authHeader) {
      api.defaults.headers.common['Authorization'] = authHeader
    }
    const response = await api.get('/auth/me', {
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
  window.removeEventListener('login-success', checkLoginStatus)
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
      await api.post('/auth/logout', null, {
        headers: { Authorization: localStorage.getItem('authHeader') },
      })
      localStorage.removeItem('authHeader')
      delete api.defaults.headers.common['Authorization']
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
  if (showNotificationModal.value) {
    const modalElement = document.querySelector('.notification-modal')
    const notificationIconBtn = notificationRef.value
    
    // 모달, 알림 아이콘 버튼 외부를 클릭한 경우에만 모달 닫기
    if (modalElement && !modalElement.contains(e.target) &&
        (!notificationIconBtn || !notificationIconBtn.contains(e.target))) {
      showNotificationModal.value = false
    }
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
  // 모든 서브스텝은 프로젝트 컨텍스트(프로젝트 ID와 step/substep 쿼리)로 이동하도록 통일
  if (isProfessorReadOnly.value && projectId.value) {
    router.push({
      path: `/professor/project/${projectId.value}`,
      query: { ...route.query, step, substep: subStepType }
    });
  } else {
    router.push({ path: '/myproject', query: { step, substep: subStepType } });
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

/* ===== 메가메뉴 알림 섹션 ===== */
.notification-mega-section {
  width: 380px;
  margin-left: auto;
  padding-left: 32px;
  border-left: 1px solid #e5e7eb;
}

.notification-mega-header {
  margin-bottom: 16px;
}

.notification-text-highlight {
  background: linear-gradient(135deg, #ff6b6b, #ff8e8e);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  text-align: center;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(255, 107, 107, 0.3);
  }
  50% {
    box-shadow: 0 2px 16px rgba(255, 107, 107, 0.5);
  }
}

.view-all-notifications-btn {
  width: 100%;
  padding: 8px 12px;
  background: var(--brand);
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.view-all-notifications-btn:hover {
  background: #2c5aa0;
}

.notification-preview-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.notification-preview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 12px;
  background: #f8f9fa;
  border-radius: 8px;
  transition: background-color 0.2s;
  cursor: pointer;
}

.notification-preview-item:hover {
  background: #e9ecef;
}

.preview-icon {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.preview-content {
  flex: 1;
  min-width: 0;
}

.preview-message {
  font-size: 13px;
  color: #333;
  margin: 0 0 4px 0;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-message strong {
  font-weight: 600;
  color: var(--brand);
}

.preview-time {
  font-size: 11px;
  color: #888;
}

.no-notifications-preview {
  text-align: center;
  padding: 32px 16px;
  color: #999;
}

.no-notifications-preview p {
  margin: 0;
  font-size: 14px;
}

.more-notifications-hint {
  text-align: center;
  padding: 8px;
  font-size: 12px;
  color: #666;
  font-style: italic;
}

/* ===== 설정/뱃지 등 간단 유지 ===== */
.settings-icon{
  position:relative; display:flex; align-items:center; gap:1rem;
  padding-right: 24px;
  margin-left: auto;
}

.notification-button-wrapper {
  position: relative;
}

.notification-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-btn svg {
  color: #333;
  transition: color 0.2s;
}

.notification-btn:hover svg {
  color: var(--brand);
}

.notification-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  background-color: #ff3b30;
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
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

/* ===== 알림 모달 스타일 ===== */
.notification-modal-overlay {
  position: fixed;
  top: 60px;
  right: 24px;
  z-index: 2000;
  pointer-events: none;
}

.notification-modal {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  width: 420px;
  max-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  animation: slideIn 0.3s ease-out;
  pointer-events: auto;
  border: 1px solid #e0e0e0;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.notification-modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.notification-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.close-modal-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #888;
  cursor: pointer;
  line-height: 1;
  padding: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-modal-btn:hover {
  color: #333;
}

.notification-modal-content {
  overflow-y: auto;
  flex: 1;
}

.no-notifications {
  text-align: center;
  padding: 60px 20px;
  color: #999;
}

.no-notifications p {
  margin: 0;
  font-size: 15px;
}

/* --- Notification List Common Styles --- */
.notification-list {
  padding: 0;
  margin: 0;
  list-style: none;
  max-height: 500px;
  overflow-y: auto;
}
.notification-item {
  display: grid;
  grid-template-columns: 40px 1fr auto;
  grid-template-areas: "icon content actions";
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  align-items: center;
  cursor: pointer;
  transition: background-color 0.2s;
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