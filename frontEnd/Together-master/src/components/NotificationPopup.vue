<template>
  <div class="notification-wrapper" ref="wrapperRef">
    <img
        src="@/assets/bell.png"
        @click="toggleNotifications"
        class="notification-icon"
        alt="알림 아이콘"
    />

    <div v-if="showNotifications" class="notification-popup">
      <p v-if="invitations.length === 0">초대 알림이 없습니다.</p>
      <ul v-else>
        <li
            v-for="invite in invitations"
            :key="invite.invitationId"
            class="invitation-item"
        >
          <div class="message">
            {{ invite.projectTitle }} 프로젝트에 초대되었습니다.
          </div>
          <div class="time">{{ formatTime(invite.createdAt) }}</div>
          <div class="actions">
            <button
                class="accept-btn"
                @click.stop="accept(invite.invitationId)"
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
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import axios from 'axios'

const showNotifications = ref(false)
const invitations = ref([])
const wrapperRef = ref(null)

function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) fetchInvitations()
}

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
          // createdAt 또는 createdDate 필드 중 사용 가능한 값으로 설정
          createdAt: inv.createdAt || inv.createdDate
        }))
  } catch (e) {
    console.error('초대 목록 조회 실패', e)
  }
}

async function accept(invitationId) {
  try {
    await axios.post(
        `/projects/invite/${invitationId}/accept`,
        null,
        { withCredentials: true }
    )
    invitations.value = invitations.value.filter(
        inv => inv.invitationId !== invitationId
    )
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
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>

<style scoped>
.notification-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.notification-icon {
  width: 24px;
  height: 24px;
  cursor: pointer;
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
