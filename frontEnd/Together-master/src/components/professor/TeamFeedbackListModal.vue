<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <div class="modal-header">
        <h3>📝 피드백 내역</h3>
        <button class="close-btn" @click="$emit('close')">✕</button>
      </div>

      <ul v-if="feedbacks.length > 0" class="feedback-list">
        <li v-for="fb in feedbacks" :key="fb.feedbackId || fb.id" class="feedback-item">
          <p class="content">{{ fb.content }}</p>
          <div class="meta">
            <span class="recipient">To: {{ fb.author || '알 수 없음' }}</span>
            <span class="date">{{ formatDate(fb.createdAt) }}</span>
          </div>
        </li>
      </ul>

      <p v-else class="empty">아직 등록된 피드백이 없습니다.</p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const props = defineProps({ projectId: Number })
const feedbacks = ref([])

onMounted(async () => {
  try {
    const res = await api.get('/professor/feedbacks', {
      params: { projectId: props.projectId }
    })
    // API 응답이 배열인 경우에만 데이터를 할당하고, 최신순으로 정렬합니다.
    if (Array.isArray(res.data)) {
      feedbacks.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else {
      feedbacks.value = [];
    }
  } catch (e) {
    console.error('피드백 내역 로딩 실패:', e)
    feedbacks.value = []
  }
})

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`;
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  padding: 24px 28px;
  width: 550px;
  max-height: 70vh;
  overflow-y: auto;
  border-radius: 12px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.modal-header h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}
.close-btn {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #888;
}
.feedback-list {
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.feedback-item {
  background-color: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 14px 16px;
}
.content {
  font-size: 14px;
  color: #343a40;
  line-height: 1.6;
  white-space: pre-wrap;
  margin-bottom: 10px;
}
.meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #868e96;
}
.recipient {
  font-weight: 500;
  color: #495057;
}
.empty {
  font-size: 14px;
  color: #868e96;
  text-align: center;
  padding: 40px 0;
}
</style>
