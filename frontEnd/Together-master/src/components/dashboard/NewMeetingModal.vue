<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <h3 class="modal-title">새로운 회의 일정 잡기</h3>
      <form @submit.prevent="submitMeeting">
        <div class="form-group">
          <label for="meeting-title">회의 제목</label>
          <input id="meeting-title" type="text" v-model="title" placeholder="예: 주간 진행 상황 공유" required />
        </div>
        <div class="form-group">
          <label for="meeting-date">날짜</label>
          <input id="meeting-date" type="date" v-model="date" required />
        </div>
        <div class="form-group">
          <label for="meeting-description">간단한 설명 (선택)</label>
          <textarea id="meeting-description" v-model="description" rows="3" placeholder="회의 안건을 간단히 적어주세요."></textarea>
        </div>
        <div class="modal-actions">
          <button type="button" class="btn-cancel" @click="close">취소</button>
          <button type="submit" class="btn-create">생성하기</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

const emit = defineEmits(['close', 'create']);

const title = ref('');
const date = ref('');
const description = ref('');

const submitMeeting = () => {
  if (!title.value || !date.value) {
    alert('회의 제목과 날짜를 모두 입력해주세요.');
    return;
  }
  emit('create', {
    title: title.value,
    date: date.value,
    description: description.value,
  });
};

const close = () => {
  emit('close');
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(0, 0, 0, 0.6);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: white;
  padding: 24px 30px;
  border-radius: 12px;
  width: 90%;
  max-width: 450px;
  box-shadow: 0 5px 15px rgba(0,0,0,0.3);
}
.modal-title {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 24px;
  text-align: center;
  color: #333;
}
.form-group {
  margin-bottom: 16px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #555;
}
.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  font-size: 14px;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-sizing: border-box;
  transition: border-color 0.2s;
}
.form-group input:focus,
.form-group textarea:focus {
  border-color: #3f8efc;
  outline: none;
}
.form-group textarea {
  resize: vertical;
  min-height: 80px;
}
.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
.modal-actions button {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-cancel {
  background-color: #f1f5f9;
  color: #475569;
}
.btn-cancel:hover {
  background-color: #e2e8f0;
}
.btn-create {
  background-color: #3f8efc;
  color: white;
}
.btn-create:hover {
  background-color: #1d6fe6;
}
</style>