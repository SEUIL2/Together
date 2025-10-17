<template>
  <div class="modal-overlay">
    <div class="modal-content">
      <h3>팀 전체 메모</h3>
      <textarea v-model="memo" placeholder="팀에 대한 메모를 작성하세요..." />

      <div class="button-group">
        <button @click="submitMemo">저장</button>
        <button class="cancel" @click="$emit('close')">닫기</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const props = defineProps({
  projectId: Number
})

const memo = ref('')

onMounted(async () => {
  try {
    const res = await api.get(`/professor/memo`, {
      params: { projectId: props.projectId }
    })
    memo.value = res.data?.content || ''
  } catch (e) {
    console.error('팀 메모 불러오기 실패:', e)
  }
})

async function submitMemo() {
  try {
    await api.post(`/professor/memo`, {
      projectId: props.projectId,
      content: memo.value
    })
    alert('저장되었습니다.')
    $emit('close')
  } catch (e) {
    console.error('팀 메모 저장 실패:', e)
    alert('저장에 실패했습니다.')
  }
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
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
}
textarea {
  width: 100%;
  height: 150px;
  margin-top: 10px;
  padding: 10px;
  resize: none;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 5px;
}
.button-group {
  margin-top: 15px;
  text-align: right;
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
