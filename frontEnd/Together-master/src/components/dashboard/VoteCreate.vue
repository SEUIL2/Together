<template>
  <div class="vote-create">
    <h3>새 투표 생성</h3>
    <input v-model="title" placeholder="투표 제목" />

    <div v-for="(opt, idx) in options" :key="idx" class="opt-row">
      <input
        v-model="options[idx]"
        placeholder="옵션 입력"
      />
      <button @click="removeOption(idx)" :disabled="options.length <= 2">–</button>
    </div>

    <button @click="addOption">옵션 추가 +</button>

    <div class="actions">
      <button @click="createVote" :disabled="!title.trim()">생성하기</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const emit = defineEmits(['created'])
const title = ref('')
const options = ref(['', ''])

function addOption() {
  options.value.push('')
}
function removeOption(i) {
  options.value.splice(i, 1)
}

async function createVote() {
  if (!title.value.trim()) return
  await axios.post(
    '/votes/create',
    { title: title.value, options: options.value.filter(o => o.trim()) },
    { headers: { Authorization: localStorage.getItem('authHeader') }, withCredentials: true }
  )
  emit('created')
}
</script>

<style scoped>
.vote-create { display: flex; flex-direction: column; gap: 8px; padding: 12px; border: 1px solid #ddd; border-radius: 6px; }
.opt-row { display: flex; gap: 4px; }
.opt-row input { flex: 1; }
.actions { margin-top: 12px; }
.actions button { padding: 6px 12px; background: #3f8efc; color: #fff; border: none; border-radius: 4px; }
</style>
