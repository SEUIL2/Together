<template>
    <div class="notification-wrapper">
      <img src="@/assets/bell.png" @click="toggleNotifications" class="notification-icon" />
  
      <div v-if="showNotifications" class="notification-popup">
        <p v-if="notifications.length === 0">알림이 없습니다.</p>
        <ul v-else>
          <li 
            v-for="note in notifications" 
            :key="note.id" 
            :class="{ unread: !note.read }"
            @click="markAsRead(note)"
          >
            {{ note.message }}
          </li>
        </ul>
      </div>
    </div>
  </template>
  
  <script setup>
  //알림팝업
  import { ref } from 'vue'
  import axios from 'axios'
  
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
      const res = await axios.get('/notifications', { withCredentials: true })
      notifications.value = res.data
    } catch (e) {
      console.error('알림 가져오기 실패', e)
    }
  }
  
  async function markAsRead(notification) {
    if (notification.read) return
  
    try {
      await axios.patch(`/notifications/${notification.id}/read`, null, {
        withCredentials: true
      })
      notification.read = true
    } catch (e) {
      console.error('알림 읽음 처리 실패', e)
    }
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
  /* 기존 스타일 유지 */
  position: absolute;
  top: 40px;
  right: 30px;
  background: white;
  border: 1px solid #ddd;
  padding: 10px;
  width: 400px;
  max-height: 300px;
  overflow-y: auto;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 2000;
  border-radius: 10px;

  /* 스크롤바 숨기기 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE, Edge */
}

.notification-popup::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

  
  .notification-popup ul {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  .notification-popup li {
    padding: 6px 0;
    font-size: 14px;
    border-bottom: 1px solid #f0f0f0;
    cursor: pointer;
  }
  
  .notification-popup li.unread {
    font-weight: bold;
    color: #3f8efc;
  }
  </style>
  