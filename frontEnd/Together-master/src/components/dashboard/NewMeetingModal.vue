<template>
  <div class="modal-overlay" @click.self="close">
    <div class="modal-content">
      <h3 class="modal-title">{{ isEditMode ? '회의 일정 수정' : '새로운 회의 일정' }}</h3>
      <form @submit.prevent="submitMeeting">
        <div class="form-group">
          <label for="meeting-title">회의 제목</label>
          <input id="meeting-title" type="text" v-model="title" placeholder="예: 주간 진행 상황 공유" required />
        </div>
        <div class="form-group-row">
          <div class="form-group">
            <label for="meeting-date">날짜</label>
            <input id="meeting-date" type="date" v-model="date" required />
          </div>
          <div class="form-group">
            <label for="meeting-time">시간</label>
            <input id="meeting-time" type="time" v-model="time" required />
          </div>
        </div>
        <div class="form-group">
          <label for="meeting-description">간단한 설명 (선택)</label>
          <textarea id="meeting-description" v-model="description" rows="3" placeholder="회의 안건을 간단히 적어주세요."></textarea>
        </div>
        <div class="modal-actions">
          <button v-if="isEditMode" type="button" class="btn-delete" @click="deleteMeeting">삭제하기</button>
          <button type="button" class="btn-cancel" @click="close">취소</button>
          <button type="submit" class="btn-create">{{ isEditMode ? '수정하기' : '생성하기' }}</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

const props = defineProps({
  initialData: {
    type: Object,
    default: null,
  },
});
const emit = defineEmits(['close', 'create', 'update', 'delete']);

const isEditMode = computed(() => !!props.initialData);

const title = ref('');
const date = ref('');
const time = ref('');
const description = ref('');

const toLocalISOString = (date) => {
  const tzoffset = (new Date()).getTimezoneOffset() * 60000; //offset in milliseconds
  const localISOTime = (new Date(date - tzoffset)).toISOString().slice(0, -1);
  return localISOTime;
}

onMounted(() => {
  if (isEditMode.value) {
    const localDateTime = toLocalISOString(new Date(props.initialData.scheduleDate));
    title.value = props.initialData.title;
    description.value = props.initialData.description;
    date.value = localDateTime.split('T')[0];
    time.value = localDateTime.split('T')[1].substring(0, 5);
  } else {
    // 새 회의 생성 시 기본값 설정
    const now = new Date();
    // 다음 시간 정각으로 설정
    now.setHours(now.getHours() + 1);
    now.setMinutes(0);
    const localDateTime = toLocalISOString(now);
    date.value = localDateTime.split('T')[0];
    time.value = localDateTime.split('T')[1].substring(0, 5);
  }
});

const submitMeeting = () => {
  if (!title.value || !date.value || !time.value) {
    alert('회의 제목, 날짜, 시간을 모두 입력해주세요.');
    return;
  }
  const payload = {
    title: title.value,
    meetingDate: `${date.value}T${time.value}:00`, // YYYY-MM-DDTHH:mm:ss 형식으로 전송
    description: description.value,
  };

  if (isEditMode.value) {
    emit('update', { ...props.initialData, ...payload });
  } else {
    emit('create', payload);
  }
  close();
};

const deleteMeeting = () => {
  emit('delete', props.initialData.id);
  close();
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
.form-group-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
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
.btn-delete {
  background-color: #ef4444;
  color: white;
  margin-right: auto;
}
.btn-delete:hover {
  background-color: #dc2626;
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