<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <h4>공지사항 작성</h4>

      <input
        v-model="newNotice.title"
        placeholder="제목을 입력하세요"
      />

      <textarea
        v-model="newNotice.content"
        placeholder="내용을 입력하세요"
      />

      <div class="modal-actions">
        <button @click="submit">등록</button>
        <button @click="$emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const emit = defineEmits(['create', 'close'])

const newNotice = ref({
  title: '',
  content: ''
})

function submit() {
  if (!newNotice.value.title || !newNotice.value.content) {
    alert('제목과 내용을 입력해주세요.')
    return
  }

  emit('create', { ...newNotice.value })
  newNotice.value.title = ''
  newNotice.value.content = ''
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  width: 600px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.modal-content input,
.modal-content textarea {
  width: 100%;
  padding: 10px;
  margin-bottom: 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.modal-content textarea {
  height: 200px;
  resize: none;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.modal-actions button {
  padding: 6px 12px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.modal-actions button:first-child {
  background: #3f8efc;
  color: white;
}

.modal-actions button:last-child {
  background: #ccc;
  color: #333;
}
</style>
