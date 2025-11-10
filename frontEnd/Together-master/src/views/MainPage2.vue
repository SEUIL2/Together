<template>
  <div class="main-container">
    <header class="top-nav" role="navigation" aria-label="주요 메뉴">
      <div class="brand" @click="goHome" role="button" tabindex="0" aria-label="TOGETHER 홈으로 이동">
        <span class="logo-dot" aria-hidden="true"></span>
        <span class="logo-text">TOGETHER</span>
      </div>
      <div class="nav-right">
        <div ref="notificationRef" class="notification-button-wrapper">
          <button class="icon-button notification-btn" @click="toggleNotificationModal">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"></path>
              <path d="M13.73 21a2 2 0 0 1-3.46 0"></path>
            </svg>
            <span v-if="unreadCount > 0" class="notification-badge">{{ unreadCount }}</span>
          </button>
        </div>
      </div>
    </header>

    <main class="hero" role="main">
      <div class="hero-inner glass">
        <p class="eyebrow">졸업작품을 위한 든든한 동반자</p>
        <h1 class="headline">TOGETHER</h1>
        <p class="tagline">팀 협업, 일정, 회의, 다이어그램까지 한 곳에서. 이제는 시작이 더 쉬워집니다.</p>
        <div class="cta-row">
          <button class="btn cta" aria-label="10초 만에 프로젝트 생성 시작" @click="goToCreate">
            10초만에 프로젝트 생성하기
            <span class="shine" aria-hidden="true"></span>
          </button>
        </div>
        <div class="trust">
          <span class="dot" aria-hidden="true"></span>
          이미 수많은 팀이 함께하고 있어요
        </div>
      </div>
    </main>

    <footer class="foot" role="contentinfo">
      © {{ new Date().getFullYear() }} TOGETHER • Better Projects, Together
    </footer>

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
  </div>
  
</template>

<script>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import api from '@/api'
import noticeIcon from '@/assets/notice.png'
import voteIcon from '@/assets/vote.png'
import todoIcon from '@/assets/todo.png'
import defaultIcon from '@/assets/bell.png'

export default {
  name: 'MainPage2',
  setup() {
    const router = useRouter();
    const currentUser = ref(null);
    const showNotificationModal = ref(false);
    const notificationRef = ref(null);
    const invitations = ref([]);
    const notifications = ref([]);
    let pollingTimer = null;

    onMounted(async () => {
      try {
        const response = await api.get('/auth/me');
        currentUser.value = response.data;
        console.log('로그인된 사용자 정보:', response.data);
      } catch (error) {
        console.warn('로그인된 사용자 정보 없음:', error?.message || error);
      }

      // 알림 폴링 시작
      fetchAllNotifications();
      pollingTimer = setInterval(fetchAllNotifications, 10000);
      
      // 외부 클릭 감지
      document.addEventListener('click', handleClickOutside);
    });

    onBeforeUnmount(() => {
      clearInterval(pollingTimer);
      document.removeEventListener('click', handleClickOutside);
    });

    const displayName = computed(() => {
      const u = currentUser.value;
      if (!u) return null;
      return (
        u.name || u.nickname || u.username || u.displayName || u.email || '사용자'
      );
    });

    const initials = computed(() => {
      if (!displayName.value) return '';
      return displayName.value
        .toString()
        .trim()
        .split(/\s+/)
        .map(p => p[0])
        .slice(0, 2)
        .join('')
        .toUpperCase();
    });

    const unreadCount = computed(() => invitations.value.length + notifications.value.length);

    const toggleNotificationModal = () => {
      showNotificationModal.value = !showNotificationModal.value;
      if (showNotificationModal.value) {
        fetchAllNotifications();
      }
    };

    const closeNotificationModal = () => {
      showNotificationModal.value = false;
    };

    const handleClickOutside = (e) => {
      if (showNotificationModal.value) {
        const modalElement = document.querySelector('.notification-modal');
        const notificationIconBtn = notificationRef.value;
        
        if (modalElement && !modalElement.contains(e.target) &&
            (!notificationIconBtn || !notificationIconBtn.contains(e.target))) {
          showNotificationModal.value = false;
        }
      }
    };

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
      } catch (e) { 
        console.error('초대 목록 조회 실패', e); 
      }
    }

    async function fetchUnreadNotifications() {
      try {
        const resp = await api.get('/notifications/unread', { withCredentials: true });
        notifications.value = Array.isArray(resp.data) ? resp.data : [];
      } catch (e) { 
        console.error('일반 알림 조회 실패', e); 
      }
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
      } catch (e) { 
        console.error('초대 수락 실패', e); 
      }
    }

    async function reject(invitationId) {
      try {
        await api.post(`/projects/invitations/${invitationId}/reject`, null, { withCredentials: true });
        fetchAllNotifications();
      } catch (e) { 
        console.error('초대 거절 실패', e); 
      }
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

    const goToCreate = () => router.push('/CreateProject');
    const goHome = () => router.push('/');

    return { 
      goToCreate, 
      goHome, 
      currentUser, 
      displayName, 
      initials,
      showNotificationModal,
      notificationRef,
      invitations,
      notifications,
      unreadCount,
      toggleNotificationModal,
      closeNotificationModal,
      getNotificationIconPath,
      accept,
      reject,
      confirm,
      formatTime
    };
  }
}
</script>

<style scoped>
/* 색상/타이포 변수 (컴포넌트 범위) */
.main-container {
  /* 라이트 기본값 (흰색 & 밝은 파란색 메인) */
  --bg-1: #ffffff;
  --bg-2: #ffffff;
  --card: rgba(255, 255, 255, 0.72);
  --card-border: rgba(15, 23, 42, 0.06);
  --card-highlight: rgba(15, 23, 42, 0.08);
  --text-1: #0f172a;  /* slate-900 */
  --text-2: #475569;  /* slate-600 */
  --accent-1: #3b82f6; /* 브랜드 블루(고정) */
  --accent-2: #60a5fa; /* 라이트 블루 */
  --accent-3: #93c5fd; /* 소프트 블루 */
  --ok: #0ea5e9;      /* 시그널 블루 */

  min-height: 100vh;
  width: 100%;
  color: var(--text-1);
  background:
    radial-gradient(1200px 600px at 80% -10%, rgba(59, 130, 246, 0.10), transparent 60%),
    radial-gradient(900px 500px at 10% 10%, rgba(147, 197, 253, 0.08), transparent 60%),
    linear-gradient(180deg, var(--bg-1), var(--bg-2));
  position: relative;
  display: flex;
  flex-direction: column;
  overflow: clip;
}

/* 배경 장식 (부드러운 그라디언트 블럽) */
.main-container::before,
.main-container::after {
  content: '';
  position: absolute;
  inset: auto;
  width: 56vmin;
  height: 56vmin;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.5;
  pointer-events: none;
  z-index: 0;
}
.main-container::before {
  background: radial-gradient(circle at 30% 30%, color-mix(in oklab, var(--accent-3) 60%, transparent), transparent 60%);
  top: -8vmin; left: -8vmin;
  animation: floatA 14s ease-in-out infinite;
}
.main-container::after {
  background: radial-gradient(circle at 60% 60%, color-mix(in oklab, var(--accent-2) 60%, transparent), transparent 60%);
  bottom: -10vmin; right: -6vmin;
  animation: floatB 18s ease-in-out infinite;
}

/* 상단 네비게이션 */
.top-nav {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 18px clamp(16px, 4vw, 40px);
  backdrop-filter: blur(8px) saturate(120%);
}
.brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  font-weight: 800;
  letter-spacing: 0.4px;
  cursor: pointer;
}
.logo-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
  box-shadow: 0 0 10px rgba(59, 130, 246, 0.55);
}
.logo-text { font-size: 1.05rem; }

.nav-right { 
  display: flex; 
  align-items: center; 
  gap: 10px; 
}

.notification-button-wrapper {
  position: relative;
}

.icon-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.icon-button:hover {
  background-color: rgba(59, 130, 246, 0.1);
}

.notification-btn {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-btn svg {
  color: var(--text-1);
  transition: color 0.2s;
}

.notification-btn:hover svg {
  color: var(--accent-1);
}

.notification-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  background-color: #ff3b30;
  color: white;
  font-size: 10px;
  font-weight: 600;
  padding: 2px 5px;
  border-radius: 10px;
  min-width: 16px;
  text-align: center;
}

/* 히어로 섹션 */
.hero {
  position: relative;
  z-index: 1;
  display: grid;
  place-items: center;
  padding: clamp(32px, 6vw, 80px) clamp(16px, 4vw, 40px);
  flex: 1 1 auto;
}
.hero-inner {
  width: min(1100px, 92vw);
  text-align: center;
  padding: clamp(24px, 4vw, 48px);
  border-radius: 20px;
  border: 1px solid var(--card-border);
  box-shadow: 0 10px 30px rgba(2, 6, 23, 0.06), inset 0 1px 0 var(--card-highlight);
}
.glass { background: var(--card); backdrop-filter: blur(14px) saturate(140%); }

.eyebrow {
  color: var(--text-2);
  font-weight: 600;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  margin-bottom: 10px;
}
.headline {
  font-size: clamp(42px, 9vw, 92px);
  line-height: 0.95;
  font-weight: 900;
  letter-spacing: -0.02em;
  background: linear-gradient(180deg, #3b82f6, #60a5fa 55%, #93c5fd);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  margin: 4px 0 12px;
  text-shadow: 0 8px 28px rgba(59, 130, 246, 0.20);
}
.tagline {
  color: var(--text-2);
  font-size: clamp(14px, 2.4vw, 18px);
  margin: 0 auto 24px;
  max-width: 760px;
}

.cta-row { display: inline-flex; gap: 12px; align-items: center; justify-content: center; }

.btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid transparent;
  font-weight: 700;
  letter-spacing: 0.2px;
  transition: transform 200ms ease, box-shadow 200ms ease, background 200ms ease, border-color 200ms ease;
  text-decoration: none;
}

.btn.small { padding: 10px 14px; font-size: 0.95rem; }

.btn.cta {
  background: linear-gradient(135deg, var(--accent-1), var(--accent-2));
  color: #ffffff;
  border-color: color-mix(in oklab, black 6%, transparent);
  box-shadow: 0 10px 24px rgba(59, 130, 246, 0.30), 0 1px 0 rgba(255, 255, 255, 0.45) inset;
}
.btn.cta:hover { transform: translateY(-2px); box-shadow: 0 14px 36px rgba(59, 130, 246, 0.40); }
.btn.cta:active { transform: translateY(0); }

/* (비활성 스타일 제거됨: CTA는 시각적으로 정상, 클릭은 noop) */

.btn.ghost {
  background: #ffffff;
  border-color: #e2e8f0;
  color: var(--text-1);
}
.btn.ghost:hover { background: #f8fafc; border-color: #cbd5e1; }

.btn .shine {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background: linear-gradient(120deg, transparent 20%, rgba(255, 255, 255, 0.35) 35%, transparent 50%);
  transform: translateX(-120%);
  animation: shine 3.2s ease-in-out infinite;
  pointer-events: none;
}

.trust { margin-top: 32px; color: var(--text-2); font-size: 0.95rem; display: inline-flex; align-items: center; gap: 8px; }
.trust .dot { width: 8px; height: 8px; margin-left: 15px;border-radius: 50%; background: #3b82f6; box-shadow: 0 0 8px rgba(59, 130, 246, 0.5); }

/* 푸터 */
.foot { color: var(--text-2); text-align: center; padding: 18px clamp(16px, 4vw, 40px); font-size: 0.9rem; }

/* 모션 */
@keyframes floatA {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(4vmin, 2vmin, 0) scale(1.05); }
}
@keyframes floatB {
  0%, 100% { transform: translate3d(0, 0, 0) scale(1); }
  50% { transform: translate3d(-3vmin, -2vmin, 0) scale(0.98); }
}
@keyframes shine {
  0% { transform: translateX(-120%); }
  80% { transform: translateX(120%); }
  100% { transform: translateX(120%); }
}

/* ===== 알림 모달 스타일 ===== */
.notification-modal-overlay {
  position: fixed;
  top: 80px;
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
  color: #3b82f6;
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

.accept-btn { 
  background-color: #3b82f6; 
  color: white; 
}

.accept-btn:hover { 
  background-color: #2563eb; 
}

.reject-btn { 
  background-color: #f1f1f1; 
  color: #555; 
}

.reject-btn:hover { 
  background-color: #e0e0e0; 
}

.confirm-btn { 
  background-color: #e9ecf3; 
  color: #3b82f6; 
}

.confirm-btn:hover { 
  background-color: #d8dbe5; 
}

/* 반응형 */
@media (max-width: 640px) {
  .btn { width: 100%; }
  .cta-row { flex-direction: column; width: 100%; }
  .notification-modal {
    width: calc(100vw - 32px);
    right: 16px;
  }
}

/* 접근성: 모션 최소화 선호 시 */
@media (prefers-reduced-motion: reduce) {
  .main-container::before,
  .main-container::after,
  .btn .shine { animation: none !important; }
}

</style>
