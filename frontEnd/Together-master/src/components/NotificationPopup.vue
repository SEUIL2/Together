<template>
  <div class="notification-wrapper" ref="wrapperRef">
    <img
        src="@/assets/bell.png"
        @click="toggleNotifications"
        class="notification-icon"
        alt="알림 아이콘"
    />

    <div v-if="showNotifications" class="notification-popup">
      <p v-if="notifications.length === 0">알림이 없습니다.</p>
      <ul v-else>
        <li
            v-for="note in notifications"
            :key="note.id"
            :class="{ unread: !note.isRead }"
        >
          <div class="message">{{ note.message }}</div>
          <div class="time">{{ formatDate(note.createdAt) }}</div>

          <!-- 초대 알림인 경우에만 수락/거절 버튼 표시 -->
          <div v-if="note.type === 'invitation'" class="actions">
            <button
                class="accept-btn"
                @click.stop="acceptInvitation(note)"
            >
              수락
            </button>
            <button
                class="reject-btn"
                @click.stop="rejectInvitation(note)"
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

const currentUserId = 8 // 실제 로그인한 사용자 ID로 교체하세요.

const showNotifications = ref(false)
const notifications = ref([])
const wrapperRef = ref(null)
let intervalId = null

// 공지 + 초대 알림 불러오기
async function fetchNotifications() {
  try {
    const [annResp, invResp] = await Promise.all([
      axios.get('/notifications/all', {
        params: { userId: currentUserId },
        withCredentials: true
      }),
      axios.get('/projects/invitations', {
        withCredentials: true
      })
    ])

    const announcements = annResp.data.map(n => ({ ...n, type: 'announcement' }))
    const invitations = invResp.data.map(inv => ({
      id: inv.invitationId,
      message: inv.message || '새로운 팀원 초대가 도착했습니다.',
      createdAt: inv.createdAt,
      isRead: inv.isRead || false,
      type: 'invitation'
    }))

    const merged = [...announcements, ...invitations]
    merged.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    notifications.value = merged
  } catch (e) {
    console.error('알림 가져오기 실패', e)
  }
}

function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) fetchNotifications()
}

function handleClickOutside(e) {
  if (wrapperRef.value && !wrapperRef.value.contains(e.target)) {
    showNotifications.value = false
  }
}

async function acceptInvitation(note) {
  try {
    await axios.post(
        `/projects/invite/${note.id}/accept/`,
        null,
        { withCredentials: true }
    )
    // 프로젝트 멤버에 자동으로 추가됨
  } catch (e) {
    console.error('초대 수락 실패', e)
  } finally {
    // 알림 목록에서 제거
    notifications.value = notifications.value.filter(n => n.id !== note.id)
  }
}

async function rejectInvitation(note) {
  try {
    await axios.post(
        `/projects/invitations/${note.id}/reject`,
        null,
        { withCredentials: true }
    )
  } catch (e) {
    console.error('초대 거절 실패', e)
  } finally {
    notifications.value = notifications.value.filter(n => n.id !== note.id)
  }
}

function formatDate(dateStr) {
  const d = new Date(dateStr)
  const yyyy = d.getFullYear()
  const mm = String(d.getMonth() + 1).padStart(2, '0')
  const dd = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mi = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${mi}`
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  intervalId = setInterval(() => {
    if (showNotifications.value) fetchNotifications()
  }, 10000)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
  clearInterval(intervalId)
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
  margin-right: 50px;
  cursor: pointer;
}

.notification-popup {
  position: absolute;
  top: 40px;
  right: 30px;
  background: #fff;
  border: 1px solid #ddd;
  padding: 10px;
  width: 400px;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  z-index: 2000;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.notification-popup::-webkit-scrollbar {
  display: none;
}

.notification-popup ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notification-popup li {
  padding: 8px 0;
  font-size: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.notification-popup li.unread {
  font-weight: bold;
  color: #3f8efc;
}

.notification-popup .message {
  margin-bottom: 4px;
}

.notification-popup .time {
  font-size: 10px;
  color: #999;
}

.actions {
  margin-top: 6px;
  display: flex;
  gap: 8px;
}

.accept-btn,
.reject-btn {
  padding: 4px 8px;
  font-size: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.accept-btn {
  background-color: #4caf50;
  color: white;
}

.reject-btn {
  background-color: #f44336;
  color: white;
}
</style>
