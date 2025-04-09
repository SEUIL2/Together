<template>
  <div class="notification-wrapper">
    <img src="@/assets/bell.png" @click="toggleNotifications" class="notification-icon" />

    <div v-if="showNotifications" class="notification-popup">
      <p v-if="notifications.length === 0">알림이 없습니다.</p>
      <ul v-else>
        <li
          v-for="note in notifications"
          :key="note.id"
          :class="{ unread: !note.isRead }"
          @click="markAsRead(note)"
        >
          <div class="message">{{ note.message }}</div>
          <div class="time">{{ formatDate(note.createdAt) }}</div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

const showNotifications = ref(false)
const notifications = ref([])

// 초대 알림을 가져오는 함수
async function fetchNotifications() {
  try {
    // 백엔드의 초대 API 호출: 로그인한 사용자의 초대 목록을 가져옴.
    const res = await axios.get('/projects/invitations', {
      withCredentials: true
    })
    // 응답 데이터를 알림 형식으로 매핑
    notifications.value = res.data.map(invite => ({
      id: invite.invitationId, // 초대 고유 ID (InvitationResponseDto의 필드명과 맞춰주세요)
      // DTO에 별도 메시지가 없다면 기본 텍스트를 사용
      message: invite.message || '새로운 팀원 초대가 도착했습니다.',
      createdAt: invite.createdAt, // 초대 생성 시간
      isRead: invite.isRead || false // 읽음 여부 (없으면 false로 처리)
    }))
  } catch (e) {
    console.error('알림 가져오기 실패', e)
  }
}

function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    fetchNotifications()
  }
}

async function markAsRead(notification) {
  if (notification.isRead) return
  // 서버에 읽음 처리를 위한 API가 있다면 여기에 호출 가능
  // 예: await axios.post(`/notifications/${notification.id}/read`, null, { withCredentials: true })
  // 현재는 단순히 로컬 상태 변경
  notification.isRead = true
}

function formatDate(dateStr) {
  const date = new Date(dateStr)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`
}

// 초대 알림 새로고침을 위해 주기적으로 fetchNotifications() 호출 (예: 10초마다)
let intervalId = null
onMounted(() => {
  intervalId = setInterval(() => {
    fetchNotifications()
  }, 10000)
})

onUnmounted(() => {
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
  background: white;
  border: 1px solid #ddd;
  padding: 10px;
  width: 400px;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 2000;
  border-radius: 10px;
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
  cursor: pointer;
}

.notification-popup li.unread {
  font-weight: bold;
  color: #3f8efc;
}

.notification-popup .message {
  margin-bottom: 2px;
}

.notification-popup .time {
  font-size: 10px;
  color: #999;
}
</style>
