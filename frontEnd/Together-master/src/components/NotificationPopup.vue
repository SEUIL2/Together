<template>
  <div class="notification-wrapper" ref="wrapperRef">
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
import { ref, onMounted, onBeforeUnmount, onUnmounted } from 'vue'
import axios from 'axios'

// 공지사항 알림 API 호출에 필요한 로그인 사용자 ID (실제 로그인 사용자의 ID 사용)
const currentUserId = 8

const showNotifications = ref(false)
const notifications = ref([])
const wrapperRef = ref(null)
let intervalId = null

// 알림 팝업 토글: 열릴 때 두 API 호출 후 결과 병합
async function fetchNotifications() {
  try {
    // 두 API를 병렬 호출 (공지사항 알림과 팀 초대 알림)
    const [notiResponse, inviteResponse] = await Promise.all([
      axios.get('/notifications/all', {
        params: { userId: currentUserId },
        withCredentials: true
      }),
      axios.get('/projects/invitations', {
        withCredentials: true
      })
    ])

    // 공지사항 알림: 각 알림 객체에 type 추가 ("announcement")
    const announcementNotifications = notiResponse.data.map(noti => ({
      ...noti,
      type: 'announcement'
    }))

    // 팀 초대 알림: InvitationResponseDto 데이터를 알림 객체 형식으로 매핑, type은 "invitation"
    const invitationNotifications = inviteResponse.data.map(invite => ({
      id: invite.invitationId, // InvitationResponseDto에 맞게 수정
      message: invite.message || '새로운 팀원 초대가 도착했습니다.',
      createdAt: invite.createdAt,
      isRead: invite.isRead || false,
      type: 'invitation'
    }))

    // 두 배열을 병합한 후 생성 시간 순(내림차순)으로 정렬
    const merged = [...announcementNotifications, ...invitationNotifications]
    merged.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    notifications.value = merged

  } catch (e) {
    console.error('알림 데이터 가져오기 실패', e)
  }
}

// 알림 팝업 토글 함수: 팝업 열릴 때 알림 갱신 호출
function toggleNotifications() {
  showNotifications.value = !showNotifications.value
  if (showNotifications.value) {
    fetchNotifications()
  }
}

// 외부 클릭 시 팝업 닫기
function handleClickOutside(event) {
  if (wrapperRef.value && !wrapperRef.value.contains(event.target)) {
    showNotifications.value = false
  }
}

// 알림 읽음 처리: type에 따라 처리 분기
async function markAsRead(notification) {
  if (notification.isRead) return

  // 공지사항 알림인 경우 API 호출
  if (notification.type === 'announcement') {
    try {
      await axios.post(`/notifications/${notification.id}/read`, null, {
        withCredentials: true
      })
    } catch (e) {
      console.error('공지사항 알림 읽음 처리 실패', e)
    }
  }
  // 팀 초대 알림은 현재 로컬에서만 읽음 처리 (추후 서버 API가 있다면 추가)
  notification.isRead = true
}

// 시간 포맷 함수: YYYY-MM-DD hh:mm 형식
function formatDate(dateStr) {
  const date = new Date(dateStr)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const min = String(date.getMinutes()).padStart(2, '0')
  return `${yyyy}-${mm}-${dd} ${hh}:${min}`
}

// 컴포넌트가 마운트될 때 외부 클릭 이벤트 등록 및 주기적 알림 갱신(예: 10초마다)
onMounted(() => {
  document.addEventListener('click', handleClickOutside)
  intervalId = setInterval(() => {
    // 팝업이 열려 있는 동안에만 알림 데이터를 주기적으로 새로고침
    if (showNotifications.value) {
      fetchNotifications()
    }
  }, 10000)
})

// 언마운트 시 이벤트 해제 및 타이머 정리
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
