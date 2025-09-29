<template>
  <aside class="sidebar" :class="{ collapsed: isCollapsed }" ref="sidebarRef">
    <!-- 토글 버튼 -->
    <div class="sidebar-header">
      <!-- 프로젝트 이름을 클릭하면 모달이 열리도록 버튼으로 변경 -->
      <button v-if="!isCollapsed" class="project-title-btn" @click="showProjectInfoModal = true">
        <span class="project-title">{{ displayProjectName }}</span>
      </button>
      <button class="toggle-btn" @click="$emit('toggle')" title="사이드바 접기/펼치기">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="15 18 9 12 15 6"></polyline></svg>
      </button>
    </div>

    <!-- 메뉴 영역 -->
    <nav class="sidebar-nav">
      <ul>
        <!-- 대시보드 -->
        <li v-if="isLoggedIn">
          <button :class="{ active: $route.path.startsWith('/DashBoard') || $route.path.startsWith('/professor/dashboard') }" @click="goMyDashBoard" :title="isCollapsed ? '대시보드' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><rect x="3" y="3" width="7" height="7"></rect><rect x="14" y="3" width="7" height="7"></rect><rect x="14" y="14" width="7" height="7"></rect><rect x="3" y="14" width="7" height="7"></rect></svg>
            <span v-if="!isCollapsed">대시보드</span>
          </button>
        </li>

        <!-- 작업 -->
        <li v-if="isLoggedIn">
          <button :class="{ active: $route.path.startsWith('/TaskPage') || $route.path.startsWith('/professor/task') }" @click="goMyTask" :title="isCollapsed ? '작업' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><polyline points="9 11 12 14 22 4"></polyline><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"></path></svg>
            <span v-if="!isCollapsed">작업</span>
          </button>
        </li>

        <!-- 일정관리 -->
        <li v-if="isLoggedIn">
          <button :class="{ active: $route.path.startsWith('/Scheduletest') || $route.path.startsWith('/professor/schedule') }" @click="goSchedule" :title="isCollapsed ? '일정관리' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            <span v-if="!isCollapsed">일정관리</span>
          </button>
        </li>

        <!-- 피드백 -->
        <li v-if="!isProfessorReadOnly && isLoggedIn">
          <button :class="{ active: $route.path.startsWith('/Feedback') || $route.path.includes('/professor/project') && $route.query.step === '피드백' }" @click="goFeedback" :title="isCollapsed ? '피드백' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"></path></svg>
            <span v-if="!isCollapsed">피드백 내역</span>
          </button>
        </li>

        <!-- 보고서 -->
        <li v-if="!isProfessorReadOnly && isLoggedIn">
          <button :class="{ active: $route.path.startsWith('/ReportPage') || $route.path.startsWith('/professor/report') }" @click="goReport" :title="isCollapsed ? '보고서' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline><line x1="16" y1="13" x2="8" y2="13"></line><line x1="16" y1="17" x2="8" y2="17"></line><polyline points="10 9 9 9 8 9"></polyline></svg>
            <span v-if="!isCollapsed">보고서</span>
          </button>
        </li>

        <!-- 회의 -->
        <li v-if="!isProfessorReadOnly && isLoggedIn">
          <button :class="{ active: $route.path.startsWith('/MeetingPage') }" @click="goMeeting" :title="isCollapsed ? '회의' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            <span v-if="!isCollapsed">회의록</span>
          </button>
        </li>

        <!-- 팀원 관리 -->
        <li v-if="!isProfessorReadOnly && isLoggedIn">
          <button
            :class="{ active: $route.path.startsWith('/TeamManagement') || $route.path.startsWith('/professor/team') }"
            @click="goTeam"
            :title="isCollapsed ? '팀원 관리' : null"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="8.5" cy="7" r="4"></circle><polyline points="17 11 19 13 23 9"></polyline></svg>
            <span v-if="!isCollapsed">팀원 관리</span>
          </button>
        </li>

        <!-- PDF 문서 추출 -->
        <li v-if="!isProfessorReadOnly && projectDetails.projectId">
          <button @click="goToPdfExportPage" :class="{ active: $route.path.startsWith('/pdf-export') }" :title="isCollapsed ? 'PDF 문서 추출' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="7 10 12 15 17 10"></polyline><line x1="12" y1="15" x2="12" y2="3"></line></svg>
            <span v-if="!isCollapsed">문서화</span>
          </button>
        </li>

        <!-- 도움말 -->
        <li v-if="!isProfessorReadOnly && isLoggedIn">
          <button :class="{ active: $route.path.startsWith('/HelpPage') }" @click="goHelp" :title="isCollapsed ? '도움말' : null">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><circle cx="12" cy="12" r="10"></circle><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3"></path><line x1="12" y1="17" x2="12.01" y2="17"></line></svg>
            <span v-if="!isCollapsed">도움말</span>
          </button>
        </li>
      </ul>
    </nav>

    <!-- 하단 설정/로그인 영역 -->
    <div class="sidebar-footer">
       <div v-if="isProfessorReadOnly" class="readonly-project-box">
        <button class="return-btn-new" @click="goBack" :title="isCollapsed ? '돌아가기' : null">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="19" y1="12" x2="5" y2="12"></line><polyline points="12 19 5 12 12 5"></polyline></svg>
          <span v-if="!isCollapsed">돌아가기</span>
        </button>
      </div>
      <!-- 학생용 프로젝트 설정 -->
      <div v-if="!isProfessorReadOnly && projectDetails.projectId" class="project-settings">
        <!-- 설정 팝업 메뉴 -->
        <div v-if="showProjectActions" class="actions-popup">
          <button class="popup-btn leave" @click="leaveProject">🚪 프로젝트 탈퇴</button>
        </div>
        <!-- 설정 버튼 -->
        <button class="settings-btn" @click.stop="toggleProjectActions">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="3"></circle><path d="M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 0 1 0 2.83 2 2 0 0 1-2.83 0l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 0 1-2 2 2 2 0 0 1-2-2v-.09A1.65 1.65 0 0 0 9 19.4a1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 0 1-2.83 0 2 2 0 0 1 0-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 0 1-2-2 2 2 0 0 1 2-2h.09A1.65 1.65 0 0 0 4.6 9a1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 0 1 0-2.83 2 2 0 0 1 2.83 0l.06.06a1.65 1.65 0 0 0 1.82.33H9a1.65 1.65 0 0 0 1-1.51V3a2 2 0 0 1 2-2 2 2 0 0 1 2 2v.09a1.65 1.65 0 0 0 1 1.51 1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 0 1 2.83 0 2 2 0 0 1 0 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82V15a1.65 1.65 0 0 0 1.51 1H21a2 2 0 0 1 2 2 2 2 0 0 1-2 2h-.09a1.65 1.65 0 0 0-1.51 1z"></path></svg>
          <span v-if="!isCollapsed">프로젝트 설정</span>
        </button>
      </div>
    </div>

    <!-- 프로젝트 정보 수정 모달 -->
    <div v-if="showProjectInfoModal" class="modal-overlay" @click.self="showProjectInfoModal = false">
      <div class="modal-content">
        <ProjectInfoCard
          :project-id="projectDetails.projectId"
          :project-name="projectDetails.projectName"
          :project-description="projectDetails.projectDescription"
          :team-members="projectDetails.teamMembers"
          :project-image-url="projectDetails.projectImageUrl"
          :is-read-only="isProfessorReadOnly"
          @project-updated="handleProjectUpdate"
        />
      </div>
    </div>
  </aside>
</template>

<script setup>
import { ref, onMounted, computed, watch, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import ProjectInfoCard from '@/components/dashboard/ProjectInfoCard.vue'

// isCollapsed prop과 toggle 이벤트를 정의합니다.
const props = defineProps({
  isCollapsed: Boolean
})
const emit = defineEmits(['toggle'])

// 헤더바의 스크립트 로직을 대부분 그대로 가져옵니다.
const router = useRouter()
const route = useRoute()

const isLoggedIn = ref(false)
const showProjectInfoModal = ref(false)
const showProjectActions = ref(false)
const sidebarRef = ref(null)

const projectDetails = reactive({
  projectId: null,
  projectName: '프로젝트 정보 로딩 중...',
  projectDescription: '',
  teamMembers: [],
  projectImageUrl: '',
})

const authHeader = localStorage.getItem('authHeader')
if (authHeader) {
  axios.defaults.headers.common['Authorization'] = authHeader
}

const goToPdfExportPage = () => {
  if (!projectDetails.projectId) return;
  router.push(`/pdf-export/${projectDetails.projectId}`);
}

async function leaveProject() {
  if (!projectDetails.projectId) return;
  if (!confirm('정말로 프로젝트를 탈퇴하시겠습니까?')) return;
  try {
    await axios.delete('/projects/leave', {
      params: { projectId: projectDetails.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    alert('프로젝트에서 성공적으로 나갔습니다.');
    router.push('/MainPage2');
  } catch (err) {
    alert('프로젝트 탈퇴에 실패했습니다.');
    console.error(err);
  }
}

const toggleProjectActions = () => {
  showProjectActions.value = !showProjectActions.value
}

const checkLoginStatus = async () => {
  const authHeader = localStorage.getItem('authHeader')
  if (authHeader) {
    axios.defaults.headers.common['Authorization'] = authHeader
    try {
      await axios.get('/auth/me', { headers: { Authorization: authHeader }, withCredentials: true })
      isLoggedIn.value = true
    } catch {
      isLoggedIn.value = false
    }
  } else {
    isLoggedIn.value = false
  }
}

const axiosConfig = {
  headers: { Authorization: localStorage.getItem('authHeader') },
  withCredentials: true,
}

const fetchProjectDetails = async () => {
  const pid = projectId.value
  if (!pid) {
    // 학생의 경우, /projects/my 에서 projectId를 먼저 가져와야 함
    try {
      const myProjectRes = await axios.get('/projects/my', axiosConfig)
      if (myProjectRes.data && myProjectRes.data.projectId) {
        await fetchDetailsById(myProjectRes.data.projectId)
      }
    } catch (error) {
      console.error("내 프로젝트 ID 로딩 실패:", error)
      projectDetails.projectName = '프로젝트 없음'
    }
    return
  }
  await fetchDetailsById(pid)
}

const fetchDetailsById = async (id) => {
  try {
    const [projectRes, planningRes, memberRes] = await Promise.all([
      axios.get(`/projects/${id}`, axiosConfig),
      axios.get(`/planning/all`, { params: { projectId: id }, ...axiosConfig }),
      axios.get(`/projects/members/students`, { params: { projectId: id }, ...axiosConfig }),
    ])

    projectDetails.projectId = projectRes.data.projectId
    projectDetails.projectName = projectRes.data.title
    projectDetails.projectImageUrl = projectRes.data.imageUrl || ''
    projectDetails.projectDescription = planningRes.data.description?.text || ''

    projectDetails.teamMembers = memberRes.data.map(m => ({ name: m.userName, id: m.userId }))
  } catch (error) {
    console.error("프로젝트 상세 정보 로딩 실패:", error)
  }
}

const handleClickOutside = (event) => {
  if (sidebarRef.value && !sidebarRef.value.contains(event.target)) {
    showProjectActions.value = false
  }
}

onMounted(() => {
  checkLoginStatus()
  window.addEventListener('login-success', checkLoginStatus)
  fetchProjectDetails()
  document.addEventListener('click', handleClickOutside)
})

const handleProjectUpdate = (updatedData) => {
  projectDetails.projectName = updatedData.title
  projectDetails.projectDescription = updatedData.description
}

/* ====== 읽기 전용/프로젝트 ====== */
const isProfessorReadOnly = computed(() => route.query.readonly === 'true')
const projectId = computed(() => {
  const fromParams = route.params.projectId
  const fromQuery = route.query.projectId
  if (typeof fromParams === 'string' || typeof fromParams === 'number') return String(fromParams)
  if (typeof fromQuery === 'string' || typeof fromQuery === 'number') return String(fromQuery)
  return ''
})
const projectTitle = computed(() => route.query.projectTitle || '')
const goBack = () => router.push('/professor/mainpage')

const displayProjectName = computed(() => {
  return projectDetails.projectName
});

/* ====== 라우팅 함수 (HeaderBar.vue와 동일) ====== */
const createGoToFunction = (studentPath, professorPathPrefix) => {
  return () => {
    if (isProfessorReadOnly.value && projectId.value) {
      router.push(`/${professorPathPrefix}/${projectId.value}?readonly=true&projectTitle=${projectTitle.value}`)
    } else {
      router.push(studentPath)
    }
  }
}

const goMyDashBoard = createGoToFunction('/DashBoard', 'professor/dashboard')
const goMeeting = createGoToFunction('/MeetingPage', 'professor/meeting')
const goTeam = createGoToFunction('/TeamManagement', 'professor/team')
const goHelp = () => router.push('/HelpPage')
const goMyTask = createGoToFunction('/TaskPage', 'professor/task')
const goSchedule = createGoToFunction('/Scheduletest', 'professor/schedule')
const goFeedback = () => {
  if (isProfessorReadOnly.value) {
    // 교수의 읽기 전용 모드에서는 MyProject 페이지의 피드백 탭으로 이동
    router.push({ path: `/professor/project/${projectId.value}`, query: { ...route.query, step: '피드백' } });
  } else {
    router.push('/Feedback');
  }
}
const goReport = createGoToFunction('/ReportPage', 'professor/report')
</script>

<style scoped>
.sidebar {
  --sidebar-bg: #fff;
  --sidebar-width: 240px;
  --logo-text-color: #000;
  --menu-text-color: #4a5568;
  --menu-text-hover-bg: #f7fafc;
  --menu-active-bg: #eef6ff;
  --menu-active-text: #3f8efc;
  --submenu-bg: #f8f9fa;
  transition: width 0.3s ease-in-out; /* 너비 변경 애니메이션 */
  width: 16.2%;
  height: 100vh;
  position: fixed;
  top: 0;
  left: 0;
  background-color: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  z-index: 1000;
  border-right: 1px solid #e2e8f0; /* 구분선 색상 조정 */
}
.sidebar.collapsed {
  width: 70px; /* 접혔을 때 너비 */
}

/* 사이드바 헤더 (프로젝트 이름, 토글 버튼) */
.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 60px; /* 헤더바와 높이 일치 */
  border-bottom: 1px solid #e2e8f0;
  flex-shrink: 0; /* 높이 고정 */
}
.sidebar.collapsed .sidebar-header {
  justify-content: center; /* 접혔을 때 토글 버튼 중앙 정렬 */
}
.project-title {
  font-weight: 700;
  font-size: 1.1rem;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-grow: 1;
}
.project-title-btn {
  background: none;
  border: none;
  padding: 0;
  margin: 0;
  cursor: pointer;
  flex-grow: 1;
}
.toggle-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: transform 0.3s ease-in-out;
}
.sidebar.collapsed .toggle-btn {
  transform: rotate(180deg);
}

.sidebar-nav {
  flex-grow: 1;
  overflow-y: auto;
  padding: 16px 0;
}
.sidebar-nav ul { list-style: none; padding: 0; margin: 0; }
.sidebar-nav ul li { padding: 2px 12px; } /* 버튼 주변 여백을 위해 li에 패딩 추가 */
.sidebar-nav ul li button {
  display: flex;
  align-items: center;
  justify-content: flex-start; /* 아이콘과 텍스트를 왼쪽에 정렬 */
  gap: 12px; /* 아이콘과 텍스트 사이 간격 */
  width: 100%;
  padding: 12px 16px; /* 좌우 패딩 조정 */
  background: none;
  border: none;
  color: var(--menu-text-color);
  font-size: 1rem;
  text-align: left;
  cursor: pointer;
  border-radius: 8px; /* 모든 버튼에 둥근 모서리 적용 */
  transition: background-color 0.2s, color 0.2s;
}
.sidebar.collapsed .sidebar-nav ul li button {
  justify-content: center; /* 아이콘만 중앙 정렬 */
  padding: 12px;
}
.sidebar-nav ul li button:hover {
  background-color: var(--menu-text-hover-bg);
  color: var(--menu-active-text);
}
.sidebar-nav ul li button.active {
  background-color: var(--menu-active-bg);
  color: var(--menu-active-text);
  font-weight: 700;
}

/* SVG 아이콘 스타일 */
.sidebar-nav ul li button svg {
  width: 20px;
  height: 20px;
  stroke: currentColor; /* 아이콘 색상이 텍스트 색상을 따라가도록 설정 */
  fill: none;
  stroke-width: 2;
  stroke-linecap: round;
  stroke-linejoin: round;
}
/* 모든 버튼의 SVG 아이콘에 공통 스타일 적용 */
.return-btn-new svg,
.settings-btn svg {
  width: 20px;
  height: 20px;
}
.sidebar-footer {
  padding: 16px;
  border-top: 1px solid #e2e8f0;
}
.action-btn {
  padding: 10px;
  background: var(--menu-text-hover-bg);
  border: none;
  border-radius: 6px;
  color: var(--menu-text-color);
  cursor: pointer;
  text-align: center;
}
.action-btn:hover {
  color: #fff;
}

.readonly-project-box { padding: 0 12px; }
.project-badge { display: flex; align-items: center; background: #fff; border-radius: 8px; padding: 4px 10px; }
.badge-left { flex: 1; margin-right: 10px; }
.project-name { font-size: 14px; font-weight: 700; color: #000; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.return-btn { background: #eee; border: 1px solid #ddd; border-radius: 6px; padding: 4px 8px; font-size: 12px; cursor: pointer; }

.return-btn-new {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  gap: 12px;
  width: 100%;
  padding: 12px 16px;
  background: none;
  border: none;
  color: var(--menu-text-color);
  font-size: 1rem;
  text-align: left;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s, color 0.2s;
}
.return-btn-new:hover {
  background-color: var(--menu-text-hover-bg);
  color: var(--menu-active-text);
}
.sidebar.collapsed .return-btn-new {
  justify-content: center;
  padding: 12px;
}
/* 모달 스타일 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 2000; /* 사이드바보다 위에 오도록 */
}

.modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 15px;
  width: 80%;
  max-width: 500px;
}

/* 프로젝트 설정 버튼 및 팝업 */
.project-settings {
  position: relative;
  margin-top: 0px;
}

.settings-btn {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 2px 16px;
  background: none;
  border: none;
  color: var(--menu-text-color);
  font-size: 1rem;
  text-align: left;
  cursor: pointer;
  border-radius: 8px;
  transition: background-color 0.2s, color 0.2s;
}
.sidebar.collapsed .settings-btn {
  justify-content: center;
  padding: 12px;
}
.settings-btn:hover {
  background-color: var(--menu-text-hover-bg);
  color: var(--menu-active-text);
}

.actions-popup {
  position: absolute;
  bottom: 100%; /* 버튼 바로 위에 표시 */
  left: 0;
  width: 100%;
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  box-shadow: 0 -4px 12px rgba(0,0,0,0.08);
  padding: 8px;
  margin-bottom: 8px;
  z-index: 10;
}
.popup-btn { width: 100%; padding: 10px 12px; background: none; border: none; border-radius: 6px; text-align: left; cursor: pointer; font-size: 0.95rem; }
.popup-btn:hover { background: #f7fafc; }
.popup-btn.leave:hover { background: #fee2e2; color: #ef4444; }
</style>
