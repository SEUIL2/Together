<template>
  <div class="dashboard-container">
    <!-- 상단 정보 -->
    <div class="dashboard-top-card">
      <div class="info-section">
        <div class="info-content">
          <span class="highlight">{{ progress }}%</span>
          <span class="label">작업 진행도</span>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
        </div>
      </div>

      <div class="info-section">
        <img src="@/assets/bellicon.png" alt="bell" />
        <div class="info-content">
          <span class="highlight">{{ notices.length }}개</span>
          <span class="label">새로운 공지사항</span>
        </div>
      </div>

      <div class="info-section no-border">
        <img src="@/assets/todo.png" alt="todo" />
        <div class="info-content">
          <span class="highlight">{{ remainingTasks }}개</span>
          <span class="label">남은 작업</span>
        </div>
      </div>
    </div>

    <!-- 중간 작업 카드 -->
    <div class="dashboard-mid">
      <div class="card">
        <AllTasksCard :tasks="tasks" />
      </div>
      <div class="card">
        <MyTasksCard :tasks="tasks" :currentUserName="currentUserName" />
      </div>
    </div>

    <!-- 하단 카드 -->
    <div class="dashboard-bottom">
      <!-- 공지사항 카드 -->
      <div class="card notice-card-wrapper">
        <div class="card-header">
          <h3 class="board-title" @click="showAllModal = true">공지사항</h3>
          <button class="create-btn" @click.stop="showCreateModal = true">+</button>
        </div>
        <NoticeList :notices="notices" @selectNotice="openNoticeDetail" />
      </div>

      <!-- 팀 투표 카드 -->
      <div class="card">
        <div class="card-header" style="display:flex; align-items:center; justify-content:space-between;">
          <h3 class="board-title" @click="showVotingListModal = true">팀 투표</h3>
          <button class="create-btn" @click="showVoteCreateModal = true">+</button>
        </div>
        <VotingList 
          @created="onVoteCreated" 
          ref="votingListRef"
          :project-id="projectId"
          :user-type="userType"
        />
      </div>

      <!-- ✅ 피드백 내역 카드 -->
      <div class="card">
        <div class="card-header">
          <h3 class="board-title" @click="showFeedbackModal = true">피드백 내역</h3>
        </div>
        <FeedbackHistoryList/>
      </div>
    </div>

    <!-- 공지 생성 모달 -->
    <NoticeCreateModal
      v-if="showCreateModal"
      :writerName="currentUserName"
      @create="handleCreateNotice"
      @close="showCreateModal = false"
    />

    <!-- 팀 투표 생성 모달 -->
    <VoteCreateModal
      v-if="showVoteCreateModal"
      @close="showVoteCreateModal = false"
      @created="onVoteCreated"
    />

    <!-- 전체 공지사항 모달 -->
    <div v-if="showAllModal" class="modal-overlay">
      <div class="modal-content notice-modal">
        <div class="modal-header">
          <h4>전체 공지사항</h4>
          <button class="close-btn" @click="showAllModal = false">×</button>
        </div>
        <NoticeList :notices="notices" @selectNotice="openNoticeDetail" />
      </div>
    </div>

    <!-- 전체 팀 투표 모달 -->
    <div v-if="showVotingListModal" class="modal-overlay">
      <div class="modal-content voting-modal">
        <div class="modal-header">
          <h4>전체 팀 투표</h4>
          <button class="close-btn" @click="showVotingListModal = false">×</button>
        </div>
        <VotingList 
          @created="onVoteCreated"
          ref="votingListRef"
          :project-id="projectId"
          @close="showVotingListModal = false"
        />
      </div>
    </div>

    <!-- 공지 상세 모달 -->
    <NoticeDetailModal
      v-if="showNoticeModal"
      :notice="selectedNotice"
      @close="showNoticeModal = false"
      @update="handleUpdateNotice"
      @delete="handleDeleteNotice"
    />

    <!-- ✅ 피드백 전체 모달 -->
    <FeedbackHistoryModal
      v-if="showFeedbackModal"
      :projectId="projectId"
      @close="showFeedbackModal = false"
    />
  </div>
</template>



<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

import NoticeList from '@/components/notice/NoticeList.vue'
import NoticeDetailModal from '@/components/notice/NoticeDetailModal.vue'
import NoticeCreateModal from '@/components/notice/NoticeCreateModal.vue'
import AllTasksCard from '@/components/dashboard/AllTasksCard.vue'
import MyTasksCard from '@/components/dashboard/MyTasksCard.vue'
import VotingList from '@/components/dashboard/VotingList.vue'
import VoteCreateModal from '@/components/dashboard/VoteCreateModal.vue'
import FeedbackHistoryModal from '@/components/feedback/FeedbackHistoryModal.vue'
import FeedbackHistoryList from '@/components/feedback/FeedbackHistoryList.vue'

const showFeedbackModal = ref(false)
const showVoteCreateModal = ref(false)
const showVotingListModal = ref(false)  // 팀 투표 전체 모달
const votingListRef = ref(null)
const route = useRoute()
const projectId = ref(route.params.projectId || null)

const currentUserName = ref('')
const tasks = ref([])
const notices = ref([])

const showNoticeModal = ref(false)
const selectedNotice = ref(null)
const showCreateModal = ref(false)
const showAllModal = ref(false)

const progress = computed(() => {
  const total = tasks.value.length
  const completed = tasks.value.filter(t => t.status === 'COMPLETED').length
  return total ? Math.round((completed / total) * 100) : 0
})

const remainingTasks = computed(() =>
  tasks.value.filter(t => t.status !== 'COMPLETED').length
)

// 공지사항 목록 불러오기
async function fetchNotices() {
  try {
    const res = await axios.get('/notices/all-notice', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    notices.value = res.data
      .sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate))
      .map(n => ({
        ...n,
        writerName: n.writerName || n.authorName || n.userName || currentUserName.value
      }))
  } catch (e) {
    console.error('공지사항 불러오기 실패:', e)
  }
}

// 공지 생성
async function handleCreateNotice(newNotice) {
  try {
    await axios.post('/notices/create', newNotice, {
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
  }
}

// 공지 수정
async function handleUpdateNotice(updated) {
  try {
    await axios.put(`/notices/update/${updated.noticeId}`, updated, {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
        'Content-Type': 'application/json'
      },
      withCredentials: true
    })
    showNoticeModal.value = false
    await fetchNotices()
  } catch (e) {
    console.error('공지사항 수정 실패:', e)
    alert('수정 중 오류가 발생했습니다.')
  }
}

// // 공지 삭제
// async function handleDeleteNotice(noticeId) {
//   if (!confirm('정말 삭제하시겠습니까?')) return
//   try {
//     await axios.delete(`/notices/delete/${noticeId}`, {
//       headers: { Authorization: localStorage.getItem('authHeader') },
//       withCredentials: true
//     })
//     showNoticeModal.value = false
//     await fetchNotices()
//   } catch (e) {
//     console.error('공지사항 삭제 실패:', e)
//     alert('삭제 중 오류가 발생했습니다.')
//   }
// }

// 초기 로드
onMounted(async () => {
  try {
    const { data: me } = await axios.get('/auth/me', { withCredentials: true })
    currentUserName.value = me.userName
    projectId.value = me.projectId

    const taskRes = await axios.get('/work-tasks/project', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    tasks.value = taskRes.data

    await fetchNotices()
  } catch (e) {
    console.error('❌ 데이터 로드 실패:', e)
  }
})

// 공지 상세 열기
function openNoticeDetail(notice) {
  selectedNotice.value = notice
  showNoticeModal.value = true
}

// 투표 생성 완료 후 VotingList 새로고침 트리거
function onVoteCreated() {
  showVoteCreateModal.value = false
  votingListRef.value?.fetchVotes()
}
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: #f5f6f8;
}

.dashboard-top-card,
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
}

.dashboard-top-card {
  display: flex;
}

.info-section {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  border-right: 1px solid #eee;
  padding: 8px;
}
.info-section.no-border {
  border-right: none;
}

.info-content .highlight {
  font-size: 24px;
}
.info-content .label {
  font-size: 12px;
}
.progress-container {
  width: 100%;
}

.progress-label {
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
}

.progress-bar {
  width: 400px;
  height: 8px;
  background: #f2f2f2;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0;
}

.progress-fill {
  height: 100%;
  background: #4a90e2;
  transition: width 0.3s ease;
}

.dashboard-mid {
  display: flex;
  gap: 16px;
}
.dashboard-bottom {
  display: flex;           /* ① flex 컨테이너로 만들기 */
  flex-direction: row;     /* ② 주 축을 가로로 설정 (기본값이기도 합니다) */
  gap: 16px;               /* 카드 사이 간격 */
  /* flex-wrap: nowrap;    필요시 줄바꿈 금지 */
}

.dashboard-bottom .card {
  flex: 1;                 /* ③ 모든 카드를 동일 너비로 분할 */
  height: 400px; /* 고정 높이 추가 */
  overflow-y: auto;
  scrollbar-width: none;      /* Firefox용 스크롤바 감춤 */
}
.dashboard-bottom .card::-webkit-scrollbar {
  display: none;
}

.card.wide {
  flex: 2;
}
.card:not(.wide) {
  flex: 1;
}

/* 공지사항 카드 헤더 */
.notice-card-wrapper {
  padding: 12px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  margin-top: -10px;
}
.board-title {
  font-size: 20px;
  cursor: pointer;
}
.create-btn {
  background: none;
  border: none;
  color: rgb(0, 0, 0);
  font-size: 24px;
  line-height: 24px;
  text-align: center;
  cursor: pointer;
}

/* 전체보기 모달 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}
.modal-content {
  background: #fff;
  padding: 16px;
  border-radius: 8px;
  width: 600px;
  max-height: 80vh;
  overflow-y: auto;
  scrollbar-width: none;
}
.modal-content::-webkit-scrollbar {
  display: none;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.close-btn {
  background: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
}
</style>