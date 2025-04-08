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
import { ref } from 'vue'
import axios from 'axios'

const currentUserId = 8 // 실제 로그인 사용자 ID로 바꿔야 함

const showNotifications = ref(false)
const notifications = ref([])

function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    fetchNotifications()
  }
}

async function fetchNotifications() {
  try {
    const res = await axios.get('/notifications/all', {
      params: { userId: currentUserId },
      withCredentials: true
    })
    notifications.value = res.data
  } catch (e) {
    console.error('알림 가져오기 실패', e)
  }
}

async function markAsRead(notification) {
  if (notification.isRead) return

  try {
    await axios.post(`/notifications/${notification.id}/read`, null, {
      withCredentials: true
    })
    notification.isRead = true
    // 원하면 이동 기능 추가 가능
    // window.location.href = notification.linkUrl
  } catch (e) {
    console.error('알림 읽음 처리 실패', e)
  }
}

// 시간 포맷: YYYY-MM-DD HH:mm
function formatDate(dateStr) {
  const date = new Date(dateStr)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`
}
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
