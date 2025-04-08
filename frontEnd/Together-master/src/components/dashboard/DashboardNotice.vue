<template>
    <div class="notice-section">
      <div class="notice-header">
        <h3>공지사항</h3>
        <button @click="openModal">+</button>
      </div>
  
      <!-- 공지사항 목록 -->
      <ul class="notice-list">
        <li v-for="notice in notices" :key="notice.noticeId">
          <div class="notice-title">
            <strong>{{ notice.title }}</strong>
          </div>
          <div class="notice-content">{{ notice.content }}</div>
          <div class="notice-date">{{ formatDate(notice.createdDate) }}</div>
        </li>
      </ul>
  
      <!-- 공지사항 등록 모달 -->
      <div v-if="showModal" class="modal-overlay">
        <div class="modal-content">
          <h4>공지사항 작성</h4>
          <input v-model="newNotice.title" placeholder="제목" />
          <textarea v-model="newNotice.content" placeholder="내용"></textarea>
          <div class="modal-actions">
            <button @click="createNotice">등록</button>
            <button @click="closeModal">취소</button>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted } from 'vue'
  import axios from 'axios'
  
  // 💡 실제 로그인 사용자 ID, 프로젝트 ID는 props나 store 등에서 받아올 수 있음
  const userId = 1
  const projectId = 1
  
  const notices = ref([])
  const newNotice = ref({ title: '', content: '' })
  const showModal = ref(false)
  
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
  
  async function createNotice() {
    if (!newNotice.value.title || !newNotice.value.content) return alert('제목과 내용을 입력해주세요')
  
    try {
      await axios.post(`/notices/create`, newNotice.value, {
        params: { userId, projectId },
        headers: { 'Content-Type': 'application/json' },
        withCredentials: true
      })
      closeModal()
      fetchNotices() // 등록 후 다시 불러오기
    } catch (e) {
      console.error('공지사항 등록 실패', e)
    }
  }
  
  function formatDate(dateStr) {
    return new Date(dateStr).toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric'
    })
  }
  
  function openModal() {
    showModal.value = true
  }
  
  function closeModal() {
    showModal.value = false
    newNotice.value = { title: '', content: '' }
  }
  </script>
  
  <style scoped>
  .notice-section {
    padding: 16px;
    background: white;
    border-radius: 10px;
    border: 1px solid #eee;
    max-height: 320px;
    overflow-y: auto;
  }
  
  .notice-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
  }
  
  .notice-list {
    list-style: none;
    padding: 0;
    margin: 0;
  }
  
  .notice-list li {
    padding: 8px 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .notice-title {
    font-weight: bold;
  }
  
  .notice-content {
    font-size: 14px;
    margin: 4px 0;
  }
  
  .notice-date {
    font-size: 12px;
    color: #999;
  }
  
  /* 모달 스타일 */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.3);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
  }
  
  .modal-content {
    background: white;
    padding: 20px;
    border-radius: 10px;
    width: 400px;
  }
  
  .modal-content input,
  .modal-content textarea {
    width: 100%;
    margin-bottom: 10px;
    padding: 8px;
    border: 1px solid #ccc;
    border-radius: 5px;
  }
  
  .modal-actions {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }
  </style>
  