<template>
  <div class="memo-overlay">
    <div class="memo-container">
      <button class="close-btn" @click="$emit('close')">&times;</button>
      <h3>{{ member.name }} 님에게 메모</h3>
      <textarea v-model="memoText" rows="6" placeholder="메모를 입력하세요"></textarea>
      <button class="save-btn" @click="saveMemo">저장</button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import axios from 'axios'

const props = defineProps({
  member: Object,
  currentUser: Object,
  projectId: Number
})
const emit = defineEmits(['close', 'saved'])
const memoText = ref('')

watch(() => props.member, m => {
  memoText.value = m.memo || ''
})

async function saveMemo() {
  const payload = {
    evaluatorId: props.currentUser.userId,
    evaluateeId: props.member.userId,
    projectId: props.projectId,
    score: 0,
    comment: memoText.value,
    evaluationDate: null
  }
  try {
    let res
    if (props.member.evaluationId) {
      res = await axios.put(
          `/evaluations/update/${props.member.evaluationId}`,
          payload,
          { withCredentials: true }
      )
    } else {
      res = await axios.post(
          '/evaluations/create',
          payload,
          { withCredentials: true }
      )
    }
    emit('saved', {
      comment: res.data.comment,
      evaluationId: res.data.evaluationId
    })
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
