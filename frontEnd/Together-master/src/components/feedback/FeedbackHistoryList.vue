<template>
  <div class="feedback-list">
    <div v-if="feedbacks.length === 0">등록된 피드백이 없습니다.</div>
    <ul v-else>
      <li v-for="fb in feedbacks" :key="fb.feedbackId" class="feedback-item">
        <p class="content">📌 {{ fb.text }}</p>
        <p class="meta">
          작성자 ID: {{ fb.authorId }} · {{ formatDate(fb.createdAt) }}
        </p>
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({})

const feedbacks = ref([])

onMounted(fetchFeedbacks)

async function fetchFeedbacks() {
  try {
    // 👤 사용자 정보 먼저 불러오기
const { data: me } = await axios.get('/auth/me', {
  headers: { Authorization: localStorage.getItem('authHeader') },
  withCredentials: true
})
console.log('🙋 사용자 정보:', me)

// 학생일 경우 mainProjectId를 사용
const projectId = me.mainProjectId || me.projectId || me.project?.projectId
console.log('📌 추출된 프로젝트 ID:', projectId)

if (!projectId) {
  console.warn('❗ 프로젝트 ID가 없습니다.')
  return
}

    // ✅ 피드백 가져오기
    const res = await axios.get('/feedbacks/my', {
      params: { projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    console.log('📥 피드백 응답:', res.data)

    feedbacks.value = res.data.sort(
      (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
    )
  } catch (e) {
    console.error('❌ 피드백 불러오기 실패:', e)
  }
}

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return date.toLocaleDateString() + ' ' + date.toLocaleTimeString()
}
</script>


<style scoped>
.feedback-list {
  padding: 8px;
  max-height: 200px;
  overflow-y: auto;
}
.feedback-item {
  border-bottom: 1px solid #ccc;
  padding: 6px 0;
}
.content {
  font-weight: 500;
}
.meta {
  font-size: 0.8rem;
  color: #666;
}
</style>
