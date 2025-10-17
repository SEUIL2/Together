<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <h2>📄 {{ notice.title }}</h2>

      <div class="notice-info">
        <p><strong>작성자:</strong> {{ notice.writerName }}</p>
        <p><strong>작성일:</strong> {{ formatDate(notice.createdDate) }}</p>
      </div>

      <div class="notice-content">
        <p>{{ notice.content }}</p>
      </div>

<div class="actions">
  <button class="delete" @click="deleteNotice">삭제</button>
  <button class="close" @click="$emit('close')">닫기</button>
</div>

    </div>
  </div>
</template>


<script setup>
import { defineProps } from 'vue'
import api from '@/api'
const props = defineProps({
  notice: Object
})

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
}
const emit = defineEmits(['close', 'delete'])


async function deleteNotice() {
  const confirmed = confirm('정말 이 공지사항을 삭제하시겠습니까?')
  if (!confirmed) return

  try {
    await api.delete(`/notices/delete/${props.notice.noticeId}`, {
      headers: {
        Authorization: localStorage.getItem('authHeader')
      },
      withCredentials: true
    })
    alert('삭제되었습니다.')
    emit('delete')
    emit('close')
  } catch (e) {
    console.error('공지사항 삭제 실패:', e)
    alert('삭제 중 오류가 발생했습니다.')
  }
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
  border-radius: 12px;
  box-shadow: 0 0 15px rgba(0, 0, 0, 0.3);
}

.notice-info p {
  margin: 5px 0;
}

.notice-content {
  margin-top: 15px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 6px;
  white-space: pre-line;
}

.actions {
  text-align: right;
  margin-top: 20px;
}

.close {
  background: #007bff;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
.delete {
  background: #e53935;
  color: white;
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  margin-right: 10px;
  cursor: pointer;
}


</style>
