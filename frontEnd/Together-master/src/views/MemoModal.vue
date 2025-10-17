<template>
  <div class="memo-overlay">
    <div class="memo-container">
      <button class="close-btn" @click="$emit('close')">&times;</button>
      <h3 class="memo-title">{{ member.userName }} 님에게 메모</h3>
      <textarea v-model="noteText" rows="6" placeholder="메모를 입력하세요"></textarea>
      <button class="save-btn" @click="saveNote">저장</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import api from '@/api'

const props = defineProps({
  member: { type: Object, required: true },
  currentUser: {type: Object, required: true},
  projectId: {type: Number, required: true}
})
const emit = defineEmits(['close', 'saved'])

const noteText = ref('')

watch(
    () => props.member.memo,
    (newMemo) => {
      noteText.value = newMemo || ''
    },
    {immediate: true}
)

async function saveNote() {
  const payload = {
    targetStudentId: props.member.userId,
    content: noteText.value
  }

  try {
    let res
    if (props.member.noteId) {
      res = await api.put(
          `/notes/update/${props.member.userId}`,
          {content: noteText.value},
          {
            withCredentials: true,
            params: {projectId: props.projectId}
          }
      )
      emit('saved', {
        content: res.data.content,
        noteId: res.data.noteId
      })
    } else {
      res = await api.post(
          '/notes/create',
          payload,
          {
            withCredentials: true,
            params: {projectId: props.projectId}
          }
      )
      emit('saved', {
        content: res.data.content,
        noteId: res.data.noteId
      })
    }
    emit('close')
  } catch (e) {
    console.error('메모 저장 실패', e)
  }
}
</script>

<style scoped>
.memo-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 200;
}

.memo-container {
  background: #ffffff;
  padding: 24px;
  border-radius: 12px;
  width: 420px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
  position: relative;
}

.close-btn {
  position: absolute;
  top: 12px;
  right: 16px;
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #333;
  cursor: pointer;
}

.memo-title {
  margin-bottom: 16px;
  font-size: 1.25rem;
  color: #1d6fe6;
}

textarea {
  width: 100%;
  margin-bottom: 16px;
  padding: 10px;
  font-size: 1rem;
  border: 1px solid #90caf9;
  border-radius: 6px;
  resize: none;
  box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: border-color 0.3s;
}

textarea:focus {
  border-color: #1976d2;
  outline: none;
}

.save-btn {
  background-color: #1d6fe6;
  color: white;
  border: none;
  padding: 10px 20px;
  font-size: 1rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.save-btn:hover {
  background-color: #1976d2;
}
</style>
