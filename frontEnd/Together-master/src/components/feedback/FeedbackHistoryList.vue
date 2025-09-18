<template>
  <div class="feedback-list">
    <div v-if="feedbacks.length === 0">등록된 피드백이 없습니다.</div>
    <ul v-else>
      <li v-for="fb in feedbacks" :key="fb.feedbackId" class="feedback-item">
        <span class="date">{{ formatDate(fb.createdAt) }}</span>
        <span class="page">{{ fb.page }}</span>
        <span class="text">{{ fb.text }}</span>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const feedbacks = ref([])

onMounted(fetchFeedbacks)

async function fetchFeedbacks() {
  try {
    // 학생 본인의 피드백을 가져오는 것이므로, projectId 없이 호출합니다.
    // 백엔드에서 현재 로그인한 사용자를 기준으로 프로젝트를 식별합니다.
    const res = await axios.get('/feedbacks/my', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    
    // API 응답이 배열인 경우에만 데이터를 처리합니다.
    if (Array.isArray(res.data)) {
      feedbacks.value = res.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else {
      feedbacks.value = [];
    }
  } catch (e) {
    console.error('❌ 피드백 불러오기 실패:', e);
    feedbacks.value = []; // 에러 발생 시 빈 배열로 초기화
  }
}

function formatDate(dateStr) {
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = date.getMonth() + 1
  const day = date.getDate()
  return `${year}. ${month}. ${day}`
}

</script>

<style scoped>
.feedback-list {
  padding: 0px 8px;
  /* max-height, overflow-y 제거! */
  background: transparent;
}
ul {
  list-style: none;
  margin: 0;
  padding: 0;
}
.feedback-item {
  display: flex;
  align-items: center;
  gap: 20px;
  border-bottom: 1px solid #e1e4e8;
  padding: 12px 0;
  font-size: 1.06rem;
}
.date {
  flex-shrink: 0;
  color: #3f51b5;
  font-size: 11px;
  font-weight: 600;
  min-width: 60px;
}
.page {
  flex-shrink: 0;
  background: #f1f3fa;
  color: #1976d2;
  border-radius: 12px;
  font-size: 10px;
  padding: 4px 18px;
  margin-right: 10px;
  font-weight: 500;
  letter-spacing: 0.2px;
}
.text {
  /* 여러 줄 표시하려면 white-space만 수정! */
  white-space: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  flex: 1;
  color: #222;
  font-size: 11px;
  line-height: 1.7;
}
</style>
