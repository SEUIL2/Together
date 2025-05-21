<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <h3>피드백 내역</h3>

      <ul v-if="feedbacks.length > 0">
        <li v-for="(fb, idx) in feedbacks" :key="idx">
          <p class="content">{{ fb.content }}</p>
          <p class="meta">{{ fb.author }} · {{ formatDate(fb.createdAt) }}</p>
        </li>
      </ul>

      <p v-else class="empty">아직 등록된 피드백이 없습니다.</p>

      <div class="button-group">
        <button @click="$emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({ projectId: Number })
const feedbacks = ref([])

onMounted(async () => {
  try {
    const res = await axios.get('/professor/feedbacks', {
      params: { projectId: props.projectId }
    })
    feedbacks.value = res.data
  } catch (e) {
    console.error(e)
    feedbacks.value = []
  }
})

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return date.toLocaleString('ko-KR')
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
  padding: 20px;
  width: 450px;
  max-height: 500px;
  overflow-y: auto;
  border-radius: 10px;
}
ul {
  padding: 0;
  list-style: none;
}
li {
  margin-bottom: 15px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}
.content {
  font-size: 14px;
  margin-bottom: 5px;
}
.meta {
  font-size: 12px;
  color: #888;
}
.empty {
  font-size: 14px;
  color: #aaa;
  text-align: center;
  margin: 30px 0;
}
.button-group {
  margin-top: 10px;
  text-align: right;
}
button {
  padding: 6px 12px;
  background-color: #3b82f6;
  color: white;
  border: none;
  border-radius: 5px;
  cursor: pointer;
}
button:hover {
  opacity: 0.9;
}
</style>
