<template>
  <div
    class="feedback-input"
    :style="{ position: 'absolute', top: y + 'px', left: x + 'px', zIndex: 999 }"
  >
    <textarea
      v-model="text"
      placeholder="피드백을 입력하세요"
    ></textarea>
    <div class="actions">
      <button @click="submit">등록</button>
      <button class="cancel" @click="$emit('close')">취소</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const props = defineProps({
  x: Number,
  y: Number,
  page: String,
  readonly: Boolean, // 🔥 읽기전용 여부 (true일 때만 등록 허용)
  projectId: Number
})

const emit = defineEmits(['submitted', 'close'])
const text = ref('')

// 피드백 등록
const submit = async () => {
  if (!text.value.trim()) {
    alert('내용을 입력해주세요.')
    return
  }

  if (!props.readonly) {
    alert('읽기전용 모드일 때만 피드백을 작성할 수 있습니다.')
    return
  }

  try {
await axios.post('/feedbacks/create', {
  page: props.page,
  x: props.x,
  y: props.y,
  text: text.value,
  projectId: props.projectId  // ✅ 여기!!
}, {
  headers: {
    Authorization: localStorage.getItem('authHeader')
  },
  withCredentials: true
})


    emit('submitted')
    emit('close')
  } catch (err) {
    console.error('❌ 피드백 저장 실패:', err)
    alert('피드백 저장에 실패했습니다.')
  }
}
</script>

<style scoped>
.feedback-input {
  width: 220px;
  background: white;
  border: 1px solid #ccc;
  border-radius: 6px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  padding: 10px;
}

textarea {
  width: 100%;
  height: 70px;
  resize: none;
  padding: 6px;
  border: 1px solid #bbb;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 8px;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}

button {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

button:hover {
  opacity: 0.9;
}

button.cancel {
  background-color: #eee;
  color: #444;
}

button:not(.cancel) {
  background-color: #3f8efc;
  color: white;
}
</style>
