<template>
  <div class="dashboard-container">
    <div class="card progress-card">
      <div class="progress-header">
        <span class="progress-title">프로젝트 진행률</span>
        <span class="progress-percentage">{{ progress }}%</span>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
    </div>

    <div class="stats-row">
      <div class="card stat-card">
        <div class="stat-icon-wrapper" style="background-color: #eef6ff;">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#3f8efc" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M22 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
        </div>
        <div class="stat-info">
          <div class="stat-title">전체 작업</div>
          <div class="stat-count">{{ tasks.length }}개</div>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon-wrapper" style="background-color: #fffbeb;">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#f59e0b" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"></polygon></svg>
        </div>
        <div class="stat-info">
          <div class="stat-title">진행중</div>
          <div class="stat-count">{{ inProgressTasksCount }}개</div>
        </div>
      </div>
      <div class="card stat-card">
        <div class="stat-icon-wrapper" style="background-color: #f0fdf4;">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#22c55e" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
        </div>
        <div class="stat-info">
          <div class="stat-title">완료</div>
          <div class="stat-count">{{ completedTasksCount }}개</div>
        </div>
      </div>
      <div class="card stat-card distribution-card" @mouseenter="tooltip.show = true" @mouseleave="tooltip.show = false" @mousemove="updateTooltipPosition">
        <div class="donut-chart-wrapper">
          <div class="donut-chart" :style="donutChartStyle"></div>
        </div>
        <div class="stat-info">
          <div class="stat-title">팀원별 작업 분배</div>
          <div class="stat-count-small">현황 보기</div>
        </div>
      </div>
      <div class="card stat-card meeting-card" @click="handleMeetingCardClick">
        <div class="stat-icon-wrapper" :style="{ backgroundColor: nextMeeting ? '#e0f2fe' : '#f3e8ff' }">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" :stroke="nextMeeting ? '#0ea5e9' : '#8b5cf6'" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
        </div>
        <div class="stat-info">
          <div class="stat-title">다음 회의 일정</div>
          <div v-if="nextMeeting" class="stat-count-small next-meeting-date">{{ formatMeetingDate(nextMeeting.scheduleDate) }}</div>
          <div v-else class="stat-count-small">일정 잡기</div>
        </div>
      </div>
    </div>

    <div class="tasks-row">
      <div class="card card-flex">
        <div class="card-header">
          <div class="card-title-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="8" y1="6" x2="21" y2="6"></line><line x1="8" y1="12" x2="21" y2="12"></line><line x1="8" y1="18" x2="21" y2="18"></line><line x1="3" y1="6" x2="3.01" y2="6"></line><line x1="3" y1="12" x2="3.01" y2="12"></line><line x1="3" y1="18" x2="3.01" y2="18"></line></svg>
            <h3 class="board-title" @click="router.push('/TaskPage')">모든 작업</h3>
          </div>
        </div>
        <div class="card-body"><AllTasksCard :tasks="tasks" /></div>
      </div>
      <div class="card card-flex">
        <div class="card-header">
          <div class="card-title-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><polyline points="16 11 18 13 22 9"></polyline></svg>
            <h3 class="board-title" @click="router.push('/TaskPage')">내 작업</h3>
          </div>
        </div>
        <div class="card-body"><MyTasksCard :tasks="tasks" :currentUserName="currentUserName" /></div>
      </div>
    </div>

    <div class="boards-row">
      <div class="card card-flex">
        <div class="card-header">
          <div class="card-title-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"></path><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"></path></svg>
            <h3 class="board-title" @click="showAllModal = true">공지사항</h3>
          </div>
          <button class="create-btn" @click.stop="showCreateModal = true">+</button>
        </div>
        <div class="card-body"><NoticeList :notices="notices" @selectNotice="openNoticeDetail" /></div>
      </div>
      <div class="card card-flex">
        <div class="card-header">
          <div class="card-title-wrapper">
            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="9 11 12 14 22 4"></polyline><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path></svg>
            <h3 class="board-title" @click="showVotingListModal = true">팀 투표</h3>
          </div>
          <button class="create-btn" @click="showVoteCreateModal = true">+</button>
        </div>
        <div class="card-body"><VotingList @created="onVoteCreated" ref="votingListRef" :project-id="projectId" /></div>
      </div>
    </div>

    <NoticeCreateModal
        v-if="showCreateModal"
        :writerName="currentUserName"
        @create="handleCreateNotice"
        @close="showCreateModal = false"
    />

    <VoteCreateModal
        v-if="showVoteCreateModal"
        @close="showVoteCreateModal = false"
        @created="onVoteCreated"
    />

    <div v-if="showAllModal" class="modal-overlay">
      <div class="modal-content notice-modal">
        <div class="modal-header">
          <h4>전체 공지사항</h4>
          <button class="close-btn" @click="showAllModal = false">×</button>
        </div>
        <NoticeList :notices="notices" @selectNotice="openNoticeDetail" />
      </div>
    </div>

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

    <NoticeDetailModal
        v-if="showNoticeModal"
        :notice="selectedNotice"
        @close="showNoticeModal = false"
        @update="handleUpdateNotice"
        @delete="handleDeleteNotice"
    />

    <div v-if="tooltip.show" class="chart-tooltip" :style="{ top: tooltip.y + 'px', left: tooltip.x + 'px' }">
      <div class="tooltip-title">작업 분배 현황</div>
      <ul>
        <li v-for="member in workDistribution" :key="member.name">
          <span class="tooltip-color" :style="{ backgroundColor: member.color }"></span>
          <span class="tooltip-name">{{ member.name }}:</span>
          <span class="tooltip-value">{{ member.count }}개 ({{ member.percentage.toFixed(1) }}%)</span>
        </li>
      </ul>
    </div>
  </div>

  <NewMeetingModal
      v-if="showMeetingModal"
      :initial-data="meetingToEdit"
      @close="showMeetingModal = false"
      @create="handleCreateMeeting"
      @update="handleUpdateMeeting"
      @delete="handleDeleteMeeting"
  />
</template>



<script setup>
import { ref, onMounted, onBeforeUnmount, computed, reactive } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/api'; // ✅ axios 대신 api 인스턴스 사용

import NoticeList from '@/components/notice/NoticeList.vue'
import NoticeDetailModal from '@/components/notice/NoticeDetailModal.vue'
import NoticeCreateModal from '@/components/notice/NoticeCreateModal.vue'
import AllTasksCard from '@/components/dashboard/AllTasksCard.vue'
import MyTasksCard from '@/components/dashboard/MyTasksCard.vue'
import VotingList from '@/components/dashboard/VotingList.vue'
import VoteCreateModal from '@/components/dashboard/VoteCreateModal.vue'
import NewMeetingModal from '@/components/dashboard/NewMeetingModal.vue'

const showVoteCreateModal = ref(false)
const showVotingListModal = ref(false)  // 팀 투표 전체 모달
const showMeetingModal = ref(false)
const meetingToEdit = ref(null)
const nextMeeting = ref(null)
const votingListRef = ref(null)
const route = useRoute()
const router = useRouter()
const projectId = ref(null)

const currentUserName = ref('')
const tasks = ref([])
const teamMembers = ref([])
const notices = ref([])
const showNoticeModal = ref(false)
const selectedNotice = ref(null)
const showCreateModal = ref(false)
const showAllModal = ref(false)
let refreshTimer

const tooltip = reactive({
  show: false,
  x: 0,
  y: 0,
});

const progress = computed(() => {
  const total = tasks.value.length
  const completed = tasks.value.filter(t => t.status === 'COMPLETED').length
  return total ? Math.round((completed / total) * 100) : 0
})

const inProgressTasksCount = computed(() => {
  return tasks.value.filter(t => t.status === 'IN_PROGRESS').length
})

const completedTasksCount = computed(() => {
  return tasks.value.filter(t => t.status === 'COMPLETED').length
})

const COLORS = ['#4A90E2', '#50E3C2', '#F5A623', '#F8E71C', '#BD10E0', '#9013FE', '#417505', '#D0021B'];

const workDistribution = computed(() => {
  if (!tasks.value.length) return [];

  const distributionMap = new Map();

  // 팀원 목록을 기반으로 초기화
  teamMembers.value.forEach((member, index) => {
    distributionMap.set(member.userName, {
      name: member.userName,
      count: 0,
      color: member.userColor || COLORS[index % COLORS.length]
    });
  });

  // 작업 할당 계산
  tasks.value.forEach(task => {
    const assignee = task.assignedUserName || '미지정';
    if (!distributionMap.has(assignee)) {
      distributionMap.set(assignee, {
        name: assignee,
        count: 0,
        color: '#B8B8B8' // 미지정 색상
      });
    }
    distributionMap.get(assignee).count++;
  });

  return Array.from(distributionMap.values())
      .filter(item => item.count > 0)
      .map(item => ({ ...item, percentage: tasks.value.length > 0 ? (item.count / tasks.value.length) * 100 : 0 }))
      .sort((a, b) => b.count - a.count);
});

function handleMeetingCardClick() {
  if (nextMeeting.value) {
    // 기존 회의 수정
    meetingToEdit.value = nextMeeting.value;
  } else {
    // 새 회의 생성
    meetingToEdit.value = null;
  }
  showMeetingModal.value = true;
}

async function handleCreateMeeting(meetingData) {
  if (!projectId.value) return;
  const scheduleDto = {
    title: meetingData.title,
    description: meetingData.description,
    scheduleDate: meetingData.meetingDate,
  };
  try {
    await api.post(`/api/meeting/schedules?projectId=${projectId.value}`, scheduleDto, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    showMeetingModal.value = false;
    await fetchMeetingSchedules();
  } catch (err) {
    console.error('회의 일정 생성 실패:', err);
    alert('회의 일정 생성에 실패했습니다.');
  }
}

async function handleUpdateMeeting(meetingData) {
  const scheduleDto = {
    title: meetingData.title,
    description: meetingData.description,
    scheduleDate: meetingData.meetingDate,
  };
  try {
    await api.put(`/api/meeting/schedules/${meetingData.id}`, scheduleDto, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    showMeetingModal.value = false;
    await fetchMeetingSchedules();
  } catch (err) {
    console.error('회의 일정 수정 실패:', err);
    alert('회의 일정 수정에 실패했습니다.');
  }
}

async function handleDeleteMeeting(scheduleId) {
  if (!confirm('이 회의 일정을 삭제하시겠습니까?')) return;
  try {
    await api.delete(`/api/meeting/schedules/${scheduleId}`, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    showMeetingModal.value = false;
    await fetchMeetingSchedules();
  } catch (err) {
    console.error('회의 일정 삭제 실패:', err);
    alert('회의 일정 삭제에 실패했습니다.');
  }
}

const formatMeetingDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', { month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', hour12: false });
};

const donutChartStyle = computed(() => {
  if (!workDistribution.value.length) {
    return { background: '#e9ecef' };
  }

  let gradientString = 'conic-gradient(';
  let currentPercentage = 0;

  workDistribution.value.forEach(member => {
    if (member.percentage > 0) {
      gradientString += `${member.color} ${currentPercentage}% ${currentPercentage + member.percentage}%, `;
      currentPercentage += member.percentage;
    }
  });

  gradientString = gradientString.slice(0, -2); // 마지막 ', ' 제거
  gradientString += ')';
  return { background: gradientString };
});

function updateTooltipPosition(event) {
  tooltip.x = event.clientX + 15;
  tooltip.y = event.clientY + 15;
}


async function fetchTasks() {
  if (!projectId.value) return;
  const taskRes = await api.get('/work-tasks/project', {
    params: { projectId: projectId.value }
  });
  tasks.value = taskRes.data
}

async function fetchTeamMembers() {
  if (!projectId.value) return;
  try {
    const { data } = await api.get('/projects/members/students', {
      params: { projectId: projectId.value }
    });
    teamMembers.value = data.filter(m => m.role === 'STUDENT').map(m => ({...m, userName: m.userName.trim()}));
  } catch (e) {
    console.error('팀원 정보 불러오기 실패:', e);
  }
}

// 공지사항 목록 불러오기
async function fetchNotices() {
  if (!projectId.value) return;
  try {
    const res = await api.get('/notices/all-notice', {
      params: { projectId: projectId.value }
    });

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

async function fetchMeetingSchedules() {
  if (!projectId.value) return;
  try {
    const { data } = await api.get('/api/meeting/schedules', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    const upcoming = data
        .filter(schedule => new Date(schedule.scheduleDate) > new Date())
        .sort((a, b) => new Date(a.scheduleDate) - new Date(b.scheduleDate));

    nextMeeting.value = upcoming.length > 0 ? upcoming[0] : null;
  } catch (err) {
    console.error('회의 일정 불러오기 실패:', err);
  }
}

// 공지 생성
async function handleCreateNotice(newNotice) {
  if (!projectId.value) return;
  try {
    await api.post(`/notices/create?projectId=${projectId.value}`, newNotice);
    showCreateModal.value = false
    await fetchNotices()
  } catch (e) {
    console.error('공지사항 생성 실패:', e)
  }
}

// 공지 수정
async function handleUpdateNotice(updated) {
  try {
    await api.put(`/notices/update/${updated.noticeId}`, updated);
    showNoticeModal.value = false
    await fetchNotices()
  } catch (e) {
    console.error('공지사항 수정 실패:', e)
    alert('수정 중 오류가 발생했습니다.')
  }
}

onMounted(async () => {
  try {
    const routeProjectId = route.params.projectId;
    const { data: me } = await api.get('/auth/me');
    currentUserName.value = me.userName;

    // 뷰를 보고 있는 프로젝트 ID를 설정합니다.
    // 교수 또는 학생이 자신의 프로젝트를 볼 때는 me.projectId를,
    // 교수가 특정 학생 프로젝트를 볼 때는 route.params.projectId를 사용합니다.
    projectId.value = routeProjectId || me.projectId;

    // projectId가 유효한 경우에만 데이터를 가져옵니다.
    if (projectId.value) {
      await fetchTeamMembers();
      await fetchTasks();
      await fetchNotices();
      await fetchMeetingSchedules();

      // 주기적으로 데이터를 새로고침하는 타이머를 설정합니다.
      refreshTimer = setInterval(async () => {
        await fetchTasks();
        await fetchNotices();
        await fetchMeetingSchedules();
      }, 10000);
    }
  } catch (e) {
    console.error('❌ 데이터 로드 실패:', e);
  }
});

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
  background-color: #f7f8fc;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.progress-card {
  padding: 20px;
}
.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 12px;
}
.progress-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.progress-percentage {
  font-size: 20px;
  font-weight: 700;
  color: #3f8efc;
}
.progress-bar {
  width: 100%;
  height: 12px;
  background-color: #e9ecef;
  border-radius: 6px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background-color: #3f8efc;
  border-radius: 6px;
  transition: width 0.5s ease-in-out;
}

.stats-row {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr 1.5fr 1.5fr;
  gap: 24px;
}
.card-title {
  font-size: 14px;
  color: #6c757d;
  margin: -4px 0 8px 0;
  text-align: center;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  height: 100px;
}
.stat-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.stat-title {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 4px;
}
.stat-count {
  font-size: 24px;
  font-weight: 700;
  color: #343a40;
}
.stat-count-small {
  font-size: 16px;
  font-weight: 600;
  color: #3f8efc;
}
.meeting-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.meeting-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.08);
}
.next-meeting-date {
  color: #0ea5e9;
}

.distribution-card {
  cursor: help; /* 툴팁이 있음을 암시하는 커서 */
}

.distribution-content {
}
.donut-chart-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
}
.donut-chart {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  transition: transform 0.3s ease;
}
.donut-chart:hover {
  transform: scale(1.05);
}
.distribution-legend {
}

.chart-tooltip {
  position: fixed;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  border-radius: 8px;
  padding: 12px 16px;
  font-size: 13px;
  z-index: 10000;
  pointer-events: none;
  transition: opacity 0.2s;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}
.tooltip-title {
  font-weight: 600;
  margin-bottom: 8px;
  border-bottom: 1px solid rgba(255,255,255,0.2);
  padding-bottom: 6px;
}
.chart-tooltip ul {
  margin: 0;
  padding: 0;
  list-style: none;
}
.chart-tooltip li {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 4px;
}
.tooltip-color {
  width: 10px;
  height: 10px;
  border-radius: 3px;
}

.tasks-row, .boards-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.card-flex {
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  margin-top: -10px;
}
.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
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

.card-body {
  flex-grow: 1;
  overflow-y: auto; /* 내용이 많을 경우 스크롤 */
  min-height: 0;
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