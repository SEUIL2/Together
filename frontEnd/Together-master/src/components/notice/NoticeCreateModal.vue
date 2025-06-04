<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <h2>+ 새 공지 작성</h2>

      <div class="form-group">
        <label>제목</label>
        <input v-model="form.title" type="text" placeholder="제목을 입력하세요" />
      </div>

      <div class="form-group">
        <label>내용</label>
        <textarea v-model="form.content" placeholder="내용을 입력하세요" rows="6" />
      </div>

      <div class="form-actions">
        <button class="save-btn" @click="submit">등록</button>
        <button class="cancel-btn" @click="$emit('close')">취소</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const props = defineProps({
  writerName: String
})
const emit = defineEmits(['create', 'close'])

const form = reactive({
  title: '',
  content: ''
})

function submit() {
  if (!form.title || !form.content) {
    alert('제목과 내용을 모두 입력해주세요.')
    return
  }

  emit('create', {
    title: form.title,
    content: form.content
  })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1100;
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal {
  background: white;
  width: 500px;
  padding: 20px;
  border-radius: 10px;
}
.form-group {
  margin-bottom: 1rem;
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 8px;
  font-size: 1rem;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
.save-btn {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 5px;
}
.cancel-btn {
  background: #ccc;
  color: black;
  padding: 8px 16px;
  border: none;
  border-radius: 5px;
}
</style>
