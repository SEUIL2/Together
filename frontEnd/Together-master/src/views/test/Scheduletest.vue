<template>
  <div class="task-board-page">
    <!-- 검색창 및 필터 -->
    <div class="search-bar">
      <input
        v-model="searchTerm"
        @input="onSearch"
        type="text"
        placeholder="작업 검색..."
      />
      <div class="filter-toggle">
        <button
          :class="{ active: filterMode === 'all' }"
          @click="setFilter('all')"
        >모든 작업</button>
        <button
          :class="{ active: filterMode === 'mine' }"
          @click="setFilter('mine')"
        >내 작업</button>
      </div>
    </div>

    <!-- Gantt 컨테이너 -->
    <div ref="ganttContainer" class="gantt-container"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import gantt from 'dhtmlx-gantt'

// 라우트 및 읽기 전용 플래그
const route = useRoute()
const isReadOnly = computed(() => route.query.readonly === 'true')
const routeProjectId = computed(() => Number(route.params.projectId))

// Gantt 컨테이너 ref
const ganttContainer = ref(null)

// 프로젝트 ID 상태
const projectId = ref(routeProjectId.value || null)

// 사용자/팀/작업 상태
const currentUser = ref('')
const rawTeamMembers = ref([])
const teamMembers = ref([])
const searchTerm = ref('')
const filterMode = ref('all')
let allRows = []

// Axios 기본 설정 및 인증 헤더
axios.defaults.baseURL = 'http://localhost:8081'
axios.defaults.withCredentials = true
axios.defaults.headers.common['Authorization'] = localStorage.getItem('authHeader')

// 1) 프로젝트 정보 로드
async function fetchProjectInfo() {
  try {
    const res = isReadOnly.value && projectId.value
      ? await axios.get(`/projects/${projectId.value}`)
      : await axios.get('/projects/my')
    projectId.value = res.data.projectId
  } catch (e) {
    console.error('프로젝트 정보 로드 실패', e)
  }
}

// 2) 현재 사용자 정보 로드
async function fetchCurrentUser() {
  if (isReadOnly.value) return
  try {
    const { data } = await axios.get('/auth/me')
    currentUser.value = data.userName.trim()
  } catch (e) {
    console.error('유저 정보 로드 실패', e)
  }
}

// 3) 팀원 정보 로드
async function fetchTeamMembers() {
  try {
    const { data } = await axios.get('/projects/members', { params: { projectId: projectId.value } })
    rawTeamMembers.value = data
    teamMembers.value = data.map(u => ({ key: u.userName, label: u.userName, userId: u.userId }))
  } catch (e) {
    console.error('팀원 정보 가져오기 실패', e)
  }
}

// 4) 트리 구조를 flat 배열로 변환
function flattenTask(task, parent = null) {
  const start = new Date(task.startDate)
  const end = new Date(task.endDate)
  const duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  const row = [{
    id: task.id,
    text: task.title,
    start_date: task.startDate,
    duration,
    assignee: task.assignedUserName,
    parent
  }]
  if (task.childTasks) task.childTasks.forEach(c => row.push(...flattenTask(c, task.id)))
  return row
}

// 5) 서버에서 작업 목록 불러오기
async function fetchTasksFromServer() {
  try {
    const res = isReadOnly.value && projectId.value
      ? await axios.get(`/work-tasks/project`, { params: { projectId: projectId.value } })
      : await axios.get('/work-tasks/project', { params: { projectId: projectId.value } })
    allRows = res.data.flatMap(t => flattenTask(t))
    onSearch()
  } catch (e) {
    console.error('작업 불러오기 실패', e)
  }
}

// Gantt 렌더링 함수
function renderGantt(rows) {
  gantt.clearAll()
  gantt.parse({ data: rows, links: [] })
  gantt.render()
}

// 검색 및 필터 처리
function onSearch() {
  let rows = allRows
  const q = searchTerm.value.trim().toLowerCase()
  if (q) rows = rows.filter(r => r.text.toLowerCase().includes(q))
  if (filterMode.value === 'mine') rows = rows.filter(r => r.assignee?.trim() === currentUser.value)
  renderGantt(rows)
}

// 필터 모드 전환
function setFilter(mode) {
  filterMode.value = mode
  onSearch()
}

// Gantt 초기 설정
function setupGantt() {
  gantt.plugins({ drag_timeline: true })
  gantt.locale.date.month_full = ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월']
  gantt.locale.date.month_short = [...gantt.locale.date.month_full]
  gantt.locale.labels.section_description = '작업 이름'
  gantt.locale.labels.section_time        = '기간'
  gantt.locale.labels.section_assignee    = '담당자'
  gantt.locale.labels.button_save         = '저장'
  gantt.locale.labels.button_cancel       = '취소'
  gantt.locale.labels.button_delete       = '삭제'

  gantt.config.scales = [
    { unit:'month', step:1, format:"%M" },
    { unit:'day',   step:1, format:"%j" }
  ]
  gantt.config.start_date  = new Date(2025,0,1)
  gantt.config.end_date    = new Date(2025,11,31)
  gantt.config.date_format = "%Y-%m-%d"

  // 읽기 모드에 따라 편집 기능 토글
  gantt.config.readonly      = isReadOnly.value
  gantt.config.drag_move     = !isReadOnly.value
  gantt.config.drag_resize   = !isReadOnly.value
  gantt.config.drag_progress = !isReadOnly.value

  // 컬럼 설정 (비읽기 모드일 때만 + 버튼)
  gantt.config.columns = [
    { name:'text',       label:'작업',   tree:true, width:130, align:'center', resize:true },
    { name:'start_date', label:'시작일', width:100, align:'center', resize:true },
    { name:'duration',   label:'기간(일)', width:60,  align:'center' },
    { name:'assignee',   label:'담당자', width:70,  align:'center', editor:'select', options:teamMembers.value },
    { name:'add',        label:'',       width:60,  template: () => isReadOnly.value ? '' : '<button onclick="gridAddTask()">+작업</button>', resize:false }
  ]

  gantt.config.lightbox.sections = [
    { name:'description', map_to:'text', type:'textarea', label:'작업 이름' },
    { name:'time',        map_to:'auto', type:'duration', label:'기간' },
    { name:'assignee',    map_to:'assignee', type:'select', options:teamMembers.value, label:'담당자' }
  ]
}

onMounted(async () => {
  await fetchProjectInfo()
  await fetchCurrentUser()
  await fetchTeamMembers()
  setupGantt()
  await nextTick()
  gantt.init(ganttContainer.value)
  await fetchTasksFromServer()

  if (!isReadOnly.value) {
    gantt.attachEvent("onAfterTaskAdd", (tempId, task) => {
      const startStr = gantt.date.date_to_str("%Y-%m-%d")(task.start_date)
      const endStr   = gantt.date.date_to_str("%Y-%m-%d")(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration }))
      const sel      = rawTeamMembers.value.find(u => u.userName === task.assignee)
      const payload  = { title: task.text, startDate: startStr, endDate: endStr, assignedUserId: sel?.userId ?? null, status: 'PENDING', parentTaskId: task.parent || null }
      axios.post('/work-tasks', payload)
        .then(({ data }) => gantt.changeTaskId(tempId, data.id))
        .catch(err => { console.error('작업 생성 실패', err); gantt.deleteTask(tempId) })
    })

    gantt.attachEvent("onAfterTaskDrag", (id, mode) => {
      if (mode !== 'move' && mode !== 'resize') return
      const task = gantt.getTask(id)
      const payload = { startDate: gantt.date.date_to_str("%Y-%m-%d")(task.start_date), endDate: gantt.date.date_to_str("%Y-%m-%d")(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration })) }
      axios.patch(`/work-tasks/${id}/schedule`, payload).catch(err => console.error('일정 업데이트 실패', err))
    })

    gantt.attachEvent("onBeforeTaskDelete", id => {
      axios.delete(`/work-tasks/${id}`).catch(err => { console.error('작업 삭제 실패', err); fetchTasksFromServer() })
      return true
    })
  }
})
</script>

<style scoped>
.task-board-page { padding:5px; background-color: #ffffff;  /* ← 이거 추가 */ }
.search-bar { display:flex; align-items:center; margin:8px 0px 4px ;}
.search-bar input { width:200px; padding:4px 8px; border:1px solid #ccc; border-radius:4px }
.filter-toggle button { margin-left:5px; padding:4px 10px; border:1px solid #ccc; border-radius:4px; background:#fff; cursor:pointer }
.filter-toggle button.active { background:#3f8efc; color:#fff; border-color:#3f8efc }
.gantt-container { width:100%; height:calc(100vh - 120px); margin:0; padding:0;}
html, body { scrollbar-width:none; -ms-overflow-style:none }
html::-webkit-scrollbar, body::-webkit-scrollbar { display:none }



</style>