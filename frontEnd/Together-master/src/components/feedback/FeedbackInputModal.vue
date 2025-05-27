<!-- 📍 src/components/feedback/FeedbackInputModal.vue -->
<template>
  <div class="feedback-modal" :style="{ top: y + 'px', left: x + 'px' }">
    <textarea
      v-model="text"
      placeholder="피드백 내용을 입력하세요"
      rows="3"
    ></textarea>
    <div class="btn-group">
      <button @click="submitFeedback" :disabled="!text.trim()">저장</button>
      <button class="cancel" @click="$emit('close')">취소</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useFeedback } from '../../composables/useFeedback'

const props = defineProps<{
  x: number
  y: number
  page: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'submitted'): void
}>()

const text = ref('')
const { createFeedback } = useFeedback()

const projectId = Number(localStorage.getItem('projectId')) // 또는 store에서 가져오기

const submitFeedback = async () => {
  await createFeedback({
    projectId,
    page: props.page,
    x: props.x,
    y: props.y,
    text: text.value.trim(),
  })
  emit('submitted')
  emit('close')
}
</script>

<style scoped>
.feedback-modal {
  position: absolute;
  background: white;
  padding: 12px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.2);
  width: 220px;
  z-index: 100;
}
textarea {
  width: 100%;
  resize: none;
  padding: 6px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
}
.btn-group {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}
button {
  padding: 4px 10px;
  font-size: 13px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  background-color: #007bff;
  color: white;
}
button.cancel {
  background-color: #aaa;
}
button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}
</style>
