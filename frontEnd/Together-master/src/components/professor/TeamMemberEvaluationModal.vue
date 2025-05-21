<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <h3>{{ member.name }} 평가 / 메모</h3>
      <textarea v-model="memo" placeholder="내용을 입력하세요..." />

      <div class="button-group">
        <button @click="submitMemo">저장</button>
        <button class="cancel" @click="$emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import axios from 'axios'

const props = defineProps({
  member: Object,
  projectId: Number
})

const memo = ref('')

async function submitMemo() {
  try {
    await axios.post('/professor/evaluation', {
      projectId: props.projectId,
      userId: props.member.userId,
      content: memo.value
    })
    alert('저장되었습니다.')
    memo.value = ''
    emitClose()
  } catch (e) {
    console.error(e)
    alert('저장에 실패했습니다.')
  }
}

function emitClose() {
  // 닫기 이벤트 전송
  memo.value = ''
  emit('close')
}

const emit = defineEmits(['close'])
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
  width: 400px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.3);
}
textarea {
  width: 100%;
  height: 120px;
  margin-top: 10px;
  padding: 10px;
  resize: none;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 5px;
}
.button-group {
  margin-top: 15px;
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
button {
  padding: 6px 12px;
  border: none;
  border-radius: 5px;
  background-color: #3b82f6;
  color: white;
  cursor: pointer;
}
button.cancel {
  background-color: #aaa;
}
button:hover {
  opacity: 0.9;
}
</style>
