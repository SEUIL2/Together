<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <!-- 헤더 -->
      <div class="modal-header">
        <h2>📢 공지사항</h2>
        <button class="create-btn" @click="showCreateModal = true">+ 새 공지 작성</button>
      </div>

      <!-- 공지 리스트 -->
      <div class="notice-list">
        <div
          class="notice-item"
          v-for="notice in notices"
          :key="notice.noticeId"
          @click="selectNotice(notice)"
        >
          <h3 class="notice-title">{{ notice.title }}</h3>
          <p class="notice-meta">
            <span class="writer">{{ notice.writerName }}</span>
            <span class="date">{{ formatDate(notice.createdDate) }}</span>
          </p>
        </div>
      </div>

      <!-- 공지 작성 모달 -->
      <NoticeCreateModal
        v-if="showCreateModal"
        :writerName="currentUserName"
        @create="handleCreate"
        @close="showCreateModal = false"
      />

      <!-- 공지 상세 모달 -->
      <NoticeDetailModal
        v-if="selectedNotice"
        :notice="selectedNotice"
        @close="selectedNotice = null"
        @delete="fetchNotices"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import NoticeCreateModal from './NoticeCreateModal.vue'
import NoticeDetailModal from './NoticeDetailModal.vue'

const props = defineProps({ projectId: Number })
const notices = ref([])
const showCreateModal = ref(false)
const selectedNotice = ref(null)
const currentUserName = ref('')

onMounted(async () => {
  await fetchUserName()
  await fetchNotices()
})

async function fetchUserName() {
  try {
    const res = await api.get('/auth/me', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    currentUserName.value = res.data.userName
  } catch (e) {
    console.error('사용자 이름 불러오기 실패:', e)
  }
}

async function fetchNotices() {
  try {
    const res = await api.get(`/notices/all-notice?projectId=${props.projectId}`, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    notices.value = res.data
      .sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate)) // 📌 최신순 정렬
      .map(n => ({
        ...n,
        writerName: n.writerName || n.authorName || n.userName || currentUserName.value
      }))
  } catch (e) {
    console.error('공지 불러오기 실패:', e)
  }
}


async function handleCreate(noticeData) {
  try {
    await api.post(`/notices/create?projectId=${props.projectId}`, noticeData, {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
        'Content-Type': 'application/json'
      },
      withCredentials: true
    })
    showCreateModal.value = false
    await fetchNotices()
  } catch (e) {
    console.error('공지사항 생성 실패:', e)
    alert('공지사항 생성 중 오류가 발생했습니다.')
  }
}

function selectNotice(notice) {
  selectedNotice.value = notice
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: center;
  
}

.modal {
  background: #fff;
  width: 620px;
  max-height: 80vh;
  padding: 20px 24px;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.25);
   overflow-y: auto;
     /* ✨ 스크롤바 숨김 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}
.notice-list::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */

}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.create-btn {
  background: #007bff;
  color: #fff;
  border: none;
  padding: 8px 12px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
}
.create-btn:hover {
  background: #005fcc;
}

.notice-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.notice-item {
  padding: 14px 16px;
  background: #f9f9f9;
  border-radius: 10px;
  border: 1px solid #e0e0e0;
  cursor: pointer;
  transition: background 0.25s;
}
.notice-item:hover {
  background: #f0f0f0;
}

.notice-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #222;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
}
</style>
