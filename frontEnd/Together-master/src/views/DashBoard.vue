<template>
  <div class="dashboard-container">
     <div class="dashboard-top">
  <!-- 프로젝트 카드 -->
  <div class="card info-card">
    <ProjectInfoCard
      :project-id="projectId"
      :project-name="projectName"
      :project-description="projectDescription"
      :team-members="teamMembers"
      :project-image-url="projectImageUrl"
      :progress="progress"
    />
  </div>

  <!-- 공지사항 -->
  <div class="card notice-card-wrapper">
    <div class="card-header">
      <h3 class="board-title" @click="showAllModal = true">공지사항</h3>
      <button class="create-btn" @click.stop="showCreateModal = true">+</button>
    </div>
    <NoticeList :notices="notices" @selectNotice="openNoticeDetail" />
  </div>

  <!-- 팀 투표 -->
  <div class="card vote-card-wrapper">
    <div class="card-header" style="display:flex; align-items:center; justify-content:space-between;">
      <h3 class="board-title" @click="showVotingListModal = true">팀 투표</h3>
      <button class="create-btn" @click="showVoteCreateModal = true">+</button>
    </div>
<div class="card-body">
    <VotingList 
      @created="onVoteCreated" 
      ref="votingListRef"
      :project-id="projectId"
      :user-type="userType"
    />
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
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

import NoticeList from '@/components/notice/NoticeList.vue'
import NoticeDetailModal from '@/components/notice/NoticeDetailModal.vue'
import NoticeCreateModal from '@/components/notice/NoticeCreateModal.vue'
import AllTasksCard from '@/components/dashboard/AllTasksCard.vue'
import MyTasksCard from '@/components/dashboard/MyTasksCard.vue'
import ProjectInfoCard from '@/components/dashboard/ProjectInfoCard.vue'
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
const projectName = ref('')
const projectDescription = ref('')
const projectImageUrl = ref('')
const teamMembers = ref([])

const showNoticeModal = ref(false)
const selectedNotice = ref(null)
const showCreateModal = ref(false)
const showAllModal = ref(false)
let refreshTimer

const progress = computed(() => {
  const total = tasks.value.length
  const completed = tasks.value.filter(t => t.status === 'COMPLETED').length
  return total ? Math.round((completed / total) * 100) : 0
})


async function fetchTasks() {
  const taskRes = await axios.get('/work-tasks/project', {
    headers: { Authorization: localStorage.getItem('authHeader') },
    withCredentials: true
  })
  tasks.value = taskRes.data
}

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

async function fetchProjectInfo() {
  try {
    const projectRes = await axios.get('/projects/my', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    projectId.value = projectRes.data.projectId
    projectName.value = projectRes.data.title
    projectImageUrl.value = projectRes.data.imageUrl || ''

    const planningRes = await axios.get('/planning/all', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    projectDescription.value = planningRes.data.description?.text || ''

    const memberRes = await axios.get('/projects/members/students', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    teamMembers.value = memberRes.data
      .filter(m => m.role === 'STUDENT')
      .map(m => ({ name: m.userName, id: m.userId }))
  } catch (e) {
    console.error('프로젝트 정보 불러오기 실패:', e)
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

onMounted(async () => {
  try {
    const { data: me } = await axios.get('/auth/me', { withCredentials: true })
    currentUserName.value = me.userName
    projectId.value = me.projectId
    await fetchProjectInfo()
    await fetchTasks()
    await fetchNotices()
    refreshTimer = setInterval(async () => {
      await fetchTasks()
      await fetchNotices()
    }, 10000)
  } catch (e) {
    console.error('❌ 데이터 로드 실패:', e)
  }
})

onBeforeUnmount(() => clearInterval(refreshTimer))

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
.card {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  width: 25%;
}
.dashboard-top {
  display: flex;
  grid-template-columns: repeat(3, 1fr); /* 같은 너비 3등분 */
  gap: 16px;
  height: 400px;
}
/* 카드가 자체 높이 안에서만 보여주도록 */
.vote-card-wrapper{
  display:flex;
  flex-direction:column;
  min-width:0;
  overflow:hidden;        /* 카드 밖으로 새는 것 차단 */
}
.vote-card-wrapper .card-header{
  flex:0 0 auto;
}
.vote-card-wrapper .card-body{
  flex:1 1 auto;          /* 남은 공간 모두 차지 */
  min-height:0;           /* 내부 스크롤 작동 핵심 */
  overflow:hidden;        /* 내부 컴포넌트가 스크롤 담당 */
}

/* ProjectInfoCard만 살짝 띄워주기 */
.info-card {
  transform: translateY(-8px); /* 위로 살짝 띄움 */
  box-shadow: 0 4px 12px rgba(0,0,0,0.1); /* 그림자로 분리된 느낌 */
  border-radius: 8px;
  margin-right: 60px;
  background-color: #bacaff;
}
.notice-card-wrapper {
height: 100%;
width: 80%;
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