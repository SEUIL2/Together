<template>
  <div class="memo-overlay">
    <div class="memo-container">
      <button class="close-btn" @click="$emit('close')">&times;</button>
      <h3>{{ member.userName }} 님에게 메모</h3>
      <!-- v-model에 기존 메모가 들어오도록 noteText를 초기화 -->
      <textarea v-model="noteText" rows="6" placeholder="메모를 입력하세요"></textarea>
      <button class="save-btn" @click="saveNote">저장</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  member: {
    type: Object,
    required: true
  },
  currentUser: {
    type: Object,
    required: true
  },
  projectId: {
    type: Number,
    required: true
  }
})
const emit = defineEmits(['close', 'saved'])

// textarea와 바인딩할 변수
const noteText = ref('')

// props.member.memo가 바뀌면 noteText에 반영
watch(
    () => props.member.memo,
    (newMemo) => {
      noteText.value = newMemo || ''
    },
    { immediate: true }
)

async function saveNote() {
  // payload로 보낼 내용
  const payload = {
    targetStudentId: props.member.userId,
    content: noteText.value
  }

  try {
    let res
    if (props.member.noteId) {
      // 이미 noteId가 있으면 수정(PUT)
      res = await axios.put(
          `/notes/update/${props.member.userId}`,
          { content: noteText.value },
          { withCredentials: true }
      )
      // 백엔드가 수정된 note를 반환한다고 가정
      emit('saved', {
        content: res.data.content,
        noteId: res.data.noteId
      })
    } else {
      // 신규 메모 생성(POST)
      res = await axios.post(
          '/notes/create',
          payload,
          { withCredentials: true }
      )
      // 백엔드가 생성된 note를 반환한다고 가정
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
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.4);
  display: flex; justify-content: center; align-items: center;
  z-index: 200;
}
.memo-container {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 400px;
  position: relative;
}
.close-btn {
  position: absolute;
  top: 8px; right: 12px;
  background: none; border: none;
  font-size: 1.2rem; cursor: pointer;
}
textarea {
  width: 100%;
  margin: 12px 0;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.save-btn {
  background-color: #4caf50;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
.save-btn:hover {
  opacity: 0.9;
}
</style>
