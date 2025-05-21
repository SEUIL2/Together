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

    <!-- 하단 영역 -->
    <div class="dashboard-bottom">
      <div class="card wide">
        <NoticeBoard :notices="notices" @selectNotice="openNoticeDetail" />
      </div>

      <div class="card">
        <VotingList />
      </div>

      <div class="card">
        <h3>최근 활동</h3>
        <p>최근 활동 예시</p>
      </div>
    </div>

    <!-- 공지사항 상세 모달 -->
    <NoticeDetailModal
      v-if="showNoticeModal"
      :notice="selectedNotice"
      :readonly="true"
      @close="showNoticeModal = false"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watchEffect } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'

import NoticeBoard from '@/components/dashboard/NoticeBoard.vue'
import NoticeDetailModal from '@/components/dashboard/NoticeDetailModal.vue'
import AllTasksCard from '@/components/dashboard/AllTasksCard.vue'
import MyTasksCard from '@/components/dashboard/MyTasksCard.vue'
import VotingList from '@/components/dashboard/VotingList.vue'

const route = useRoute()
const isProfessorReadOnly = route.query.readonly === 'true'
const projectId = ref(route.params.projectId || null)
const projectTitle = route.query.projectTitle || ''

const currentUserName = ref('')
const currentUserId = ref('')
const tasks = ref([])
const notices = ref([])

const showNoticeModal = ref(false)
const selectedNotice = ref(null)

const progress = computed(() => {
  const total = tasks.value.length
  const done = tasks.value.filter(t => t.status === 'COMPLETED').length
  return total ? Math.round((done / total) * 100) : 0
})

const remainingTasks = computed(() => {
  return tasks.value.filter(t => t.status !== 'COMPLETED').length
})

onMounted(async () => {
  try {
    if (isProfessorReadOnly && projectId.value) {
      const { data } = await axios.get(`/work-tasks/project/${projectId.value}`, {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
      tasks.value = data
    } else {
      const { data } = await axios.get('/auth/me', { withCredentials: true })
      currentUserName.value = data.userName?.trim()
      currentUserId.value = data.userId
      projectId.value = data.projectId

      const taskRes = await axios.get('/work-tasks/project', {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
      tasks.value = taskRes.data
    }

    const noticeRes = await axios.get('/notices/all-notice', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    notices.value = noticeRes.data
  } catch (e) {
    console.error('❌ 작업 또는 공지사항 데이터 불러오기 실패:', e)
  }
})

function openNoticeDetail(notice) {
  selectedNotice.value = notice
  showNoticeModal.value = true
}

watchEffect(() => {
  console.log('✅ [대시보드] currentUserName:', currentUserName.value)
  console.log('✅ [대시보드] tasks:', tasks.value)
})
</script>


  
  <style scoped>
.dashboard-container {
  padding: 30px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: #f5f6f8; /* 전체 배경 연회색 */
  min-height: 100vh;
}

/* 모든 카드 스타일 공통 */
.card, .dashboard-top-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  border: none; /* 테두리 제거로 더 깔끔 */
}

/* 상단 박스 */
.dashboard-top-card {
  display: flex;
  overflow: hidden;
}

/* 작업 중간/하단 박스 간격 */
.dashboard-mid,
.dashboard-bottom {
  display: flex;
  gap: 20px;
}

/* 카드 너비 */
.card.wide {
  flex: 2;
}
.card:not(.wide) {
  flex: 1;
}

  
  .info-section {
  flex: 1;
  padding: 0px 24px;
  border-right: 1px solid #eee;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start; /* 👉 왼쪽 정렬로 변경 */
  gap: 12px;
}

  
  .info-section.no-border {
    border-right: none;
  }
  
  .info-section img {
    width: 36px;
  }
  
  .info-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1px; /* 간격 추가 */
  min-width: 240px; /* ✅ progress-bar를 넣을 공간 확보 */
}

  
  .highlight {
    font-size: 28px;
    color: #3f8efc;
    font-weight: bold;
    line-height: 1.2;
  }
  
  .label {
    font-size: 14px;
    color: #777;
    line-height: 1.2;
  }
  
  .progress-bar {
  width: 100%; /* 예시로 200~300px */
  height: 10px;
  background: #ddd;
  border-radius: 5px;
  overflow: hidden;
}

  
  .progress-fill {
    height: 100%;
    background: #3f8efc;
    border-radius: 10px;
  }
  
  /* 중간/하단 카드 동일 */
  .dashboard-mid {
    display: flex;
    gap: 20px;
  }
  .card {
    background: white;
    border: 1px solid #ddd;
    border-radius: 10px;
    padding: 16px;
    flex: 1;
  }
  .dashboard-bottom {
    display: flex;
    gap: 20px;
  }
  .card.wide {
    flex: 1;
  }
  .card:not(.wide) {
    flex: 1;
  }
  </style>