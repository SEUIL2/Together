<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <div class="modal-header">
        <h4>공지사항</h4>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <input
        v-model="localNotice.title"
        :readonly="readonly || !isEditing"
        placeholder="제목을 입력하세요"
      />

      <textarea
        v-model="localNotice.content"
        :readonly="readonly || !isEditing"
        placeholder="내용을 입력하세요"
      />

      <p class="writer-info">작성자: {{ localNotice.writerName }}</p>

      <div class="modal-actions">
        <template v-if="!readonly">
          <button v-if="!isEditing" @click="isEditing = true">수정</button>
          <button v-if="isEditing" @click="onSave">저장</button>
          <button @click="onDelete">삭제</button>
        </template>
        <button @click="$emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  notice: { type: Object, required: true },
  readonly: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'update', 'delete'])

const isEditing = ref(false)
const localNotice = ref({ ...props.notice })

watch(
  () => props.notice,
  (newVal) => {
    localNotice.value = { ...newVal }
    isEditing.value = false
  }
)

function onSave() {
  emit('update', { ...localNotice.value })
  // 모달 자동 닫기
  emit('close')
}

function onDelete() {
    emit('delete', localNotice.value.noticeId)
    emit('close')
  
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

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
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

.modal-actions button:nth-child(2) {
  background: #f44336;
  color: white;
}

.modal-actions button:last-child {
  background: #ccc;
  color: #333;
}

.writer-info {
  font-size: 13px;
  color: #666;
  margin-top: -8px;
  margin-bottom: 16px;
  text-align: right;
}
</style>