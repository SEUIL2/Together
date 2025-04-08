<template>
  <div class="notice-section">
    <div class="notice-header">
      <h3>공지사항</h3>
      <button @click="openCreateModal">+</button>
    </div>

    <!-- 공지사항 목록 (제목만) -->
    <ul class="notice-list">
      <li
        v-for="notice in notices"
        :key="notice.noticeId"
        @click="openDetailModal(notice)"
        class="notice-item"
      >
        <div class="notice-content">
          <span class="notice-title">{{ notice.title }}</span>
          <span class="notice-date">{{ formatDate(notice.createdDate) }}</span>
        </div>
      </li>
    </ul>

    <!-- 등록/수정 공용 모달 -->
    <div v-if="showModal" class="modal-overlay">
      <div class="modal-content">
        <h4>{{ isEditMode ? '공지사항' : '공지사항 작성' }}</h4>

        <!-- 제목 -->
        <input
          v-model="editNotice.title"
          placeholder="제목을 입력해주세요.."
          :readonly="isEditMode && !isEditing"
        />

        <!-- 내용 -->
        <textarea
          v-model="editNotice.content"
          placeholder="내용을 입력해주세요.."
          :readonly="isEditMode && !isEditing"
        ></textarea>

        <div class="modal-actions">
          <template v-if="isEditMode">
            <button v-if="!isEditing" @click="isEditing = true">수정</button>
            <button v-if="isEditing" @click="updateNotice">저장</button>
            <button @click="deleteNotice">삭제</button>
          </template>

          <template v-else>
            <button @click="createNotice">등록</button>
          </template>

          <button @click="closeModal">닫기</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'

const userId = 8
const projectId = 1

const notices = ref([])
const showModal = ref(false)
const isEditMode = ref(false)
const isEditing = ref(false)
const editNotice = ref({})

onMounted(() => {
  fetchNotices()
})

async function fetchNotices() {
  try {
    const res = await axios.get(`/notices/project/${projectId}`)
    notices.value = res.data
  } catch (e) {
    console.error('공지사항 불러오기 실패', e)
  }
}

function openCreateModal() {
  editNotice.value = { title: '', content: '' }
  isEditMode.value = false
  isEditing.value = true
  showModal.value = true
}

function openDetailModal(notice) {
  editNotice.value = { ...notice }
  isEditMode.value = true
  isEditing.value = false
  showModal.value = true
}

function closeModal() {
  showModal.value = false
  editNotice.value = {}
  isEditMode.value = false
  isEditing.value = false
}

async function createNotice() {
  if (!editNotice.value.title || !editNotice.value.content) return alert('제목과 내용을 입력해주세요')
  try {
    await axios.post(`/notices/create`, editNotice.value, {
      params: { userId, projectId },
      headers: { 'Content-Type': 'application/json' },
      withCredentials: true
    })
    closeModal()
    fetchNotices()
  } catch (e) {
    console.error('공지사항 등록 실패', e)
  }
}

async function updateNotice() {
  try {
    await axios.put(`/notices/update/${editNotice.value.noticeId}`, editNotice.value, {
      headers: { 'Content-Type': 'application/json' },
      withCredentials: true
    })
    closeModal()
    fetchNotices()
  } catch (e) {
    console.error('공지사항 수정 실패', e)
  }
}

async function deleteNotice() {
  try {
    await axios.delete(`/notices/delete/${editNotice.value.noticeId}`, { withCredentials: true })
    closeModal()
    fetchNotices()
  } catch (e) {
    console.error('공지사항 삭제 실패', e)
  }
}

function formatDate(dateStr) {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}월${day}일`
}
</script>

<style scoped>
.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding-bottom: 4px;
  border-bottom: 1px solid #ddd;
}

.notice-header button {
  background: transparent;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #888;
  padding: 0;
  transition: transform 0.2s ease, color 0.2s ease;
}

.notice-header button:hover {
  color: #3f8efc;
  transform: scale(1.2);
}

.notice-header h3 {
  font-size: 16px;
  line-height: 1.2;
  margin: 0;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-item {
  padding: 12px 4px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
  border-radius: 6px;
}

.notice-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-1px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.notice-item:not(:last-child) {
  border-bottom: none;
}

.notice-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-title {
  font-weight: bold;
  color: #333;
}

.notice-date {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 12px;
  width: 800px;
  height: 500px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.modal-content input {
  width: 100%;
  height: 40px;
  margin-bottom: 12px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.modal-content textarea {
  width: 100%;
  height: 300px;
  resize: none;
  margin-bottom: 12px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
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
</style>
