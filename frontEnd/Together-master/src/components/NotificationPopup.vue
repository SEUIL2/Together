<template>
  <div class="notification-wrapper" ref="wrapperRef">
    <!-- 종(벨) 아이콘 -->
    <img
        src="@/assets/bell.png"
        @click="toggleNotifications"
        class="notification-icon"
        alt="알림 아이콘"
    />

    <!-- 읽지 않은 알림(초대 + 일반)이 하나라도 있으면 파란 점 표시 -->
    <span v-if="unreadCount > 0" class="unread-dot"></span>

    <div v-if="showNotifications" class="notification-popup">
      <p v-if="invitations.length === 0 && notifications.length === 0">알림이 없습니다.</p>
      <ul v-else>
        <li
        v-for="invite in invitations"
        :key="`invite-${invite.invitationId}`"
        class="invitation-item"
        >␊
        <div class="message">
          {{ invite.projectTitle }} 프로젝트에 초대되었습니다.
        </div>
          <div class="time">{{ formatTime(invite.createdAt) }}</div>
          <div class="actions">
            <button
            class="accept-btn"
            @click.stop="accept(invite)"
            >
              수락
            </button>
            <button
                class="reject-btn"
                @click.stop="reject(invite.invitationId)"
            >
              거절
            </button>
          </div>
        </li>
        <li
            v-for="noti in notifications"
            :key="`noti-${noti.id}`"
            class="notification-item"
        >
          <div class="message">{{ noti.message }}</div>
          <div class="time">{{ formatTime(noti.createdAt) }}</div>
          <div class="actions">
            <button class="confirm-btn" @click.stop="confirm(noti)">확인</button>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

const showNotifications = ref(false)
const invitations = ref([])
const notifications = ref([])
const wrapperRef = ref(null)
const router = useRouter()

// 페이지 로딩 직후에도 unread-dot 표시를 위해, mounted 시 한 번만 알림 목록을 가져옵니다.
async function fetchInvitations() {
  try {
    const resp = await axios.get('/projects/invitations', {
      withCredentials: true
    })
    const list = Array.isArray(resp.data) ? resp.data : []
    invitations.value = list
        .filter(inv => inv.status === 'PENDING')
        .map(inv => ({
          ...inv,
          createdAt: inv.createdAt || inv.createdDate
        }))
  } catch (e) {
    console.error('초대 목록 조회 실패', e)
  }
}

async function fetchUnreadNotifications() {
  try {
    const resp = await axios.get('/notifications/unread', { withCredentials: true })
    notifications.value = Array.isArray(resp.data) ? resp.data : []
  } catch (e) {
    console.error('일반 알림 조회 실패', e)
  }
}

function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  // 팝업을 열 때마다 최신 목록으로 갱신
  if (showNotifications.value) {
    fetchInvitations()
    fetchUnreadNotifications()
  }
}

async function accept(invitation) {
  try {
    await axios.post(
        `/projects/invite/${invitation.invitationId}/accept`,
        null,
        { withCredentials: true }
    )
    invitations.value = invitations.value.filter(
        inv => inv.invitationId !== invitation.invitationId
    )

    const { data: me } = await axios.get('/auth/me', {
      withCredentials: true
    })
    const roles = Array.isArray(me.roles) ? me.roles : []
    const isProfessor = roles.some(r => r.authority === 'ROLE_PROFESSOR')

    if (isProfessor) {
      const res = await axios.get('/projects/my-projects/sorted-by-created', {
        withCredentials: true
      })
      const projects = Array.isArray(res.data) ? res.data : []
      const matched = projects.find(p => p.title === invitation.projectTitle)
      if (matched && matched.projectId) {
        router.push(
            `/professor/project/${matched.projectId}?readonly=true&projectTitle=${encodeURIComponent(matched.title)}`
        )
      } else {
        router.push('/professor/mainpage')
      }
    } else {
      router.push('/MyProject')
    }
  } catch (e) {
    console.error('초대 수락 실패', e)
  }
}

async function reject(invitationId) {
  try {
    await axios.post(
        `/projects/invitations/${invitationId}/reject`,
        null,
        { withCredentials: true }
    )
    invitations.value = invitations.value.filter(
        inv => inv.invitationId !== invitationId
    )
  } catch (e) {
    console.error('초대 거절 실패', e)
  }
}

async function confirm(noti) {
  try {
    await axios.post(`/notifications/${noti.id}/read`, null, { withCredentials: true })
    notifications.value = notifications.value.filter(n => n.id !== noti.id)
    router.push('/dashboard')
  } catch (e) {
    console.error('알림 확인 실패', e)
  }
}


function formatTime(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d)) return ''
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${hh}:${mm}`
}

function handleClickOutside(e) {
  if (wrapperRef.value && !wrapperRef.value.contains(e.target)) {
    showNotifications.value = false
  }
}

onMounted(() => {
  // 컴포넌트가 마운트되면 곧바로 초대 목록을 가져와 unread-dot 표시
  fetchInvitations()
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})

const unreadCount = computed(() => invitations.value.length + notifications.value.length)
</script>

<style scoped>
.notification-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

/* 종(벨) 아이콘 */
.notification-icon {
  width: 24px;
  height: 24px;
  cursor: pointer;
}

/* 읽지 않은 알림이 있을 때 표시할 파란 점(뱃지) */
.unread-dot {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background-color: #3f8efc;
  border-radius: 50%;
  border: 1px solid #fff;
  z-index: 1;
}

.notification-popup {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 8px;
  background: #fff;
  border: 1px solid #ddd;
  padding: 12px;
  width: 320px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  z-index: 1000;
}

.notification-popup ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.invitation-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.invitation-item:last-child {
  border-bottom: none;
}

.notification-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.notification-item:last-child {
  border-bottom: none;
}

.message {
  font-size: 14px;
  color: #333;
}

.time {
  font-size: 12px;
  color: #666;
}

.actions {
  display: flex;
  gap: 8px;
}

.accept-btn,
.reject-btn {
  flex: 1;
  padding: 6px 0;
  font-size: 12px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.accept-btn {
  background-color: #4caf50;
  color: #fff;
}

.reject-btn {
  background-color: #f44336;
  color: #fff;
}
</style>
