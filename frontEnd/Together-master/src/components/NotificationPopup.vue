<template>
  <div class="notification-wrapper" ref="wrapperRef">
    <img
        src="@/assets/bell.png"
        @click="toggleNotifications"
        class="notification-icon"
        alt="알림 아이콘"
    />
    <span v-if="unreadCount > 0" class="unread-dot"></span>

    <transition name="popup-fade">
      <div v-if="showNotifications" class="notification-popup">
        <div class="popup-header">
          <h3>알림</h3>
        </div>
        <div v-if="invitations.length === 0 && notifications.length === 0" class="no-notifications">
          <p>새로운 알림이 없습니다.</p>
        </div>
        <ul v-else class="notification-list">
          <li v-for="invite in invitations" :key="`invite-${invite.invitationId}`" class="notification-item">
            <img src="@/assets/defaultimage.png" alt="profile" class="profile-img"/>
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

          <li v-for="noti in notifications" :key="`noti-${noti.id}`" class="notification-item">
            <img src="@/assets/defaultimage.png" alt="profile" class="profile-img"/>
            <div class="notification-content">
              <div v-if="noti.type === 'NEW_NOTICE'" class="preview-content">
                <p class="preview-header">📢 새로운 공지사항</p>
                <p class="preview-title">{{ noti.previewTitle }}</p>
              </div>
              <div v-else-if="noti.type === 'NEW_VOTE'" class="preview-content">
                <p class="preview-header">🗳️ 새로운 투표</p>
                <p class="preview-title">{{ noti.previewTitle }}</p>
              </div>
              <div v-else>
                <p class="message">{{ noti.message }}</p>
              </div>
              <span class="time">{{ formatTime(noti.createdAt) }}</span>
            </div>
            <div class="actions">
              <button class="confirm-btn" @click.stop="confirm(noti)">확인</button>
            </div>
          </li>
        </ul>
      </div>
    </transition>
  </div>
</template>

<script setup>
// script 부분은 confirm 함수만 수정되었습니다.
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const showNotifications = ref(false)
const invitations = ref([])
const notifications = ref([])
const wrapperRef = ref(null)
const router = useRouter()
let pollingTimer = null

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

function toggleNotifications() {
  showNotifications.value = !showNotifications.value;
  if (showNotifications.value) {
    fetchAllNotifications();
  }
}

async function accept(invitation) {
  try {
    await axios.post(`/projects/invite/${invitation.invitationId}/accept`, null, { withCredentials: true });
    invitations.value = invitations.value.filter(inv => inv.invitationId !== invitation.invitationId);
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
    invitations.value = invitations.value.filter(inv => inv.invitationId !== invitationId);
  } catch (e) { console.error('초대 거절 실패', e); }
}

// ===== '확인' 버튼 클릭 시 라우팅 로직 수정 =====
async function confirm(noti) {
  try {
    await axios.post(`/notifications/${noti.id}/read`, null, { withCredentials: true });
    notifications.value = notifications.value.filter(n => n.id !== noti.id);

    // 알림 타입에 따라 다른 경로로 이동
    if (noti.type === 'NEW_NOTICE') {
      // relatedId를 사용하여 공지사항 상세 페이지로 이동
      router.push(`/notice/${noti.relatedId}`);
    } else if (noti.type === 'NEW_VOTE') {
      // relatedId를 사용하여 투표 상세 페이지로 이동
      router.push(`/vote/${noti.relatedId}`);
    } else {
      // 기본 경로는 대시보드
      router.push('/dashboard');
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

function handleClickOutside(event) {
  if (wrapperRef.value && !wrapperRef.value.contains(event.target)) {
    showNotifications.value = false;
  }
}

onMounted(() => {
  fetchAllNotifications();
  pollingTimer = setInterval(fetchAllNotifications, 10000);
  document.addEventListener('click', handleClickOutside);
});

onBeforeUnmount(() => {
  clearInterval(pollingTimer);
  document.removeEventListener('click', handleClickOutside);
});

const unreadCount = computed(() => invitations.value.length + notifications.value.length);
</script>

<style scoped>
/* 기존 스타일은 그대로 유지 */
.notification-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.notification-icon {
  width: 28px;
  height: 28px;
  cursor: pointer;
}
.unread-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 9px;
  height: 9px;
  background-color: #ff3b30;
  border-radius: 50%;
  border: 2px solid white;
  z-index: 2;
}
.notification-popup {
  position: absolute;
  width: 380px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  z-index: 1000;
  border: 1px solid #e8e8e8;
  right: calc(100% + 10px);
  top: 0;
}
.popup-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.popup-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.notification-list {
  padding: 8px 0;
  margin: 0;
  list-style: none;
  max-height: 450px;
  overflow-y: auto;
}
.notification-item {
  display: grid;
  grid-template-columns: 40px 1fr;
  grid-template-rows: auto auto;
  grid-template-areas:
    "avatar content"
    "avatar actions";
  column-gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}
.notification-item:last-child {
  border-bottom: none;
}
.notification-item:hover {
  background-color: #f9f9f9;
}
.profile-img {
  grid-area: avatar;
  width: 40px;
  height: 40px;
  border-radius: 50%;
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
  margin-top: 12px;
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
  flex: 1;
  border: none;
  padding: 9px 12px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}
.accept-btn { background-color: #5a76f3; color: white; }
.accept-btn:hover { background-color: #4862d1; }
.reject-btn { background-color: #f1f1f1; color: #555; }
.reject-btn:hover { background-color: #e0e0e0; }
.confirm-btn { background-color: #5a76f3; color: white; }
.confirm-btn:hover { background-color: #4862d1; }
.no-notifications {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}
.popup-fade-enter-active, .popup-fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.popup-fade-enter-from, .popup-fade-leave-to {
  opacity: 0;
  transform: translateY(-5px);
}

/* ===== 미리보기 UI 스타일 추가 ===== */
.preview-content {
  display: flex;
  flex-direction: column;
  gap: 4px; /* 간격 */
}
.preview-header {
  font-size: 12px;
  font-weight: 500;
  color: #888;
  margin: 0;
}
.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
/* ===== 미리보기 UI 스타일 끝 ===== */
</style>