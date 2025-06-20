<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h2>📋 피드백 내역</h2>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <div v-if="feedbacks.length === 0" class="empty-text">피드백이 없습니다.</div>

      <ul class="feedback-list" v-else>
        <li v-for="fb in feedbacks" :key="fb.feedbackId" class="feedback-item">
          <span class="date">{{ formatDate(fb.createdAt) }}</span>
          <span class="page">{{ fb.page }}</span>

          <div class="tooltip-container">
            <div class="text-preview">{{ truncateText(fb.text, 50) }}</div>
            <div class="tooltip">{{ fb.text }}</div>
          </div>

          <span class="status-badge" :class="{ read: fb.isRead, unread: !fb.isRead }">
            {{ fb.isRead ? '읽음' : '안읽음' }}
          </span>

          <button v-if="!fb.isRead" class="read-btn" @click="markAsRead(fb.feedbackId)">읽음</button>
          <button class="delete-btn" @click="deleteFeedback(fb.feedbackId)">삭제</button>
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import axios from 'axios'

const props = defineProps({
  projectId: Number
})
const emit = defineEmits(['close'])

const feedbacks = ref([])
const currentUserId = ref(null)

onMounted(async () => {
  try {
    const { data: me } = await axios.get('/auth/me', { withCredentials: true })
    currentUserId.value = me.userId
    console.log('🙋 사용자 ID:', currentUserId.value)
    await loadFeedbacks(me.role) // 사용자 역할도 함께 전달
  } catch (err) {
    console.error('🙅 사용자 정보 불러오기 실패:', err)
  }
})

const loadFeedbacks = async (role) => {
  try {
    let res

    if (role === 'PROFESSOR') {
      // 교수는 전체 피드백 조회
      res = await axios.get('/feedbacks/project', {
        params: { projectId: props.projectId, page: 'all' },
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
    } else {
      // 학생은 자신의 피드백만 조회
      res = await axios.get('/feedbacks/my', {
        params: { projectId: props.projectId },
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
    }

    feedbacks.value = res.data.sort(
      (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
    )

    console.log('📥 피드백 응답:', res.data)
    feedbacks.value.forEach(fb => {
      console.log(`🧾 [${fb.feedbackId}] (${fb.page}) ${fb.text} | 작성자: ${fb.authorId} | isRead: ${fb.isRead}`)
    })
  } catch (err) {
    console.error('❌ 피드백 내역 불러오기 실패:', err)
  }
}


const deleteFeedback = async (id) => {
  if (!confirm('정말 삭제하시겠습니까?')) return
  try {
    await axios.delete(`/feedbacks/${id}`, {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== id)
  } catch (err) {
    console.error('❌ 피드백 삭제 실패:', err)
    alert('삭제에 실패했습니다: ' + (err.response?.data?.message || err.message))
  }
}

const markAsRead = async (feedbackId) => {
  try {
    await axios.post(`/feedbacks/${feedbackId}/read`, null, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    feedbacks.value = feedbacks.value.map(fb =>
      fb.feedbackId === feedbackId ? { ...fb, isRead: true } : fb
    )
  } catch (err) {
    console.error('❌ 읽음 처리 실패:', err)
  }
}

const formatDate = (isoDate) => {
  const d = new Date(isoDate)
  return `${d.getMonth() + 1}월 ${d.getDate()}일 / ${d.getHours()}시 ${String(d.getMinutes()).padStart(2, '0')}분`
}

const truncateText = (text, length = 50) =>
  text.length > length ? text.slice(0, length) + '...' : text
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  border-radius: 10px;
  padding: 24px 28px;
  width: 800px;
  height: 80vh;
  overflow-y: auto;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  font-family: 'Noto Sans KR', sans-serif;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #555;
}
.close-btn:hover {
  color: #000;
}
.empty-text {
  text-align: center;
  color: #888;
  margin-top: 40px;
}
.feedback-list {
  list-style: none;
  padding: 0;
  margin: 0;
}
.feedback-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  gap: 10px;
}
.date {
  width: 120px;
  flex-shrink: 0;
  color: #666;
}
.page {
  width: 80px;
  flex-shrink: 0;
  color: #007bff;
  font-weight: 500;
}
.tooltip-container {
  position: relative;
  max-width: 300px;
  flex: 1;
}
.text-preview {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #222;
  cursor: help;
}
.tooltip {
  display: none;
  position: absolute;
  top: 120%;
  left: 0;
  background-color: #fff;
  border: 1px solid #ccc;
  padding: 10px 14px;
  max-width: 400px;
  white-space: pre-wrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  font-size: 13px;
  color: #222;
  border-radius: 6px;
  z-index: 10000;
}
.tooltip-container:hover .tooltip {
  display: block;
}
.status-badge {
  flex-shrink: 0;
  margin-left: auto;
  padding: 3px 8px;
  border-radius: 8px;
  font-size: 12px;
  width: fit-content;
}
.status-badge.read {
  background-color: #e0f2ff;
  color: #007bff;
}
.status-badge.unread {
  background-color: #ffecec;
  color: #e53935;
}
.delete-btn {
  background: none;
  border: none;
  color: #888;
  font-size: 13px;
  cursor: pointer;
  transition: color 0.2s;
  margin-left: 12px;
}
.delete-btn:hover {
  color: #e53935;
}
</style>
  