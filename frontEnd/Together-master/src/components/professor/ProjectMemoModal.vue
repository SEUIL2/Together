<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <h2>🧾 프로젝트 메모</h2>

      <div class="memo-list">
        <div v-for="memo in memos" :key="memo.id" class="memo-item">
          <div v-if="editId !== memo.id">
            <p class="content">{{ memo.content }}</p>
            <p class="date">{{ formatDate(memo.updatedAt || memo.createdAt) }}</p>
            <div class="item-actions">
              <button @click="startEdit(memo)">수정</button>
              <button class="delete" @click="deleteMemo(memo.id)">삭제</button>
            </div>
          </div>
          <div v-else class="edit-block">
            <textarea v-model="editContent" />
            <div class="item-actions">
              <button @click="updateMemo(memo.id)">저장</button>
              <button class="cancel" @click="cancelEdit">취소</button>
            </div>
          </div>
        </div>
      </div>

      <div class="create-section">
        <textarea v-model="newContent" placeholder="새 메모 작성..." />
        <button @click="createMemo">추가</button>
      </div>

      <button class="close-btn" @click="$emit('close')">닫기</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'

const props = defineProps({
  projectId: Number
})

const memos = ref([])
const newContent = ref('')
const editId = ref(null)
const editContent = ref('')

onMounted(fetchMemos)

async function fetchMemos() {
  try {
    const res = await api.get(`/memos/project/${props.projectId}`, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    memos.value = res.data || []
  } catch (e) {
    console.error('메모 목록 조회 실패:', e)
  }
}

async function createMemo() {
  if (!newContent.value.trim()) return
  try {
    const res = await api.post(`/memos?projectId=${props.projectId}`, newContent.value, {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
        'Content-Type': 'text/plain'
      },
      withCredentials: true
    })
    memos.value.push(res.data)
    newContent.value = ''
  } catch (e) {
    console.error('메모 생성 실패:', e)
  }
}

function startEdit(memo) {
  editId.value = memo.id
  editContent.value = memo.content
}

function cancelEdit() {
  editId.value = null
  editContent.value = ''
}

async function updateMemo(memoId) {
  try {
    const res = await api.put(`/memos/${memoId}`, editContent.value, {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
        'Content-Type': 'text/plain'
      },
      withCredentials: true
    })
    const idx = memos.value.findIndex(m => m.id === memoId)
    if (idx !== -1) memos.value[idx] = res.data
    cancelEdit()
  } catch (e) {
    console.error('메모 수정 실패:', e)
  }
}

async function deleteMemo(memoId) {
  if (!confirm('메모를 삭제하시겠습니까?')) return
  try {
    await api.delete(`/memos/${memoId}`, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    memos.value = memos.value.filter(m => m.id !== memoId)
  } catch (e) {
    console.error('메모 삭제 실패:', e)
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('ko-KR')
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal {
  background: #fff;
  width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  padding: 20px;
  border-radius: 10px;
}
.memo-item {
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 10px;
}
.memo-item .content {
  white-space: pre-wrap;
  margin-bottom: 6px;
}
.memo-item .date {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
}
.item-actions {
  display: flex;
  gap: 6px;
}
.item-actions button {
  padding: 4px 8px;
  font-size: 12px;
}
.item-actions button.delete {
  background-color: #e74c3c;
  color: white;
}
.item-actions button.cancel {
  background-color: #aaa;
  color: white;
}
.create-section {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
textarea {
  width: 100%;
  min-height: 80px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  resize: vertical;
}
.close-btn {
  margin-top: 10px;
  padding: 6px 12px;
}
</style>