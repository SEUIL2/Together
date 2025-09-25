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
      <div class="category-filter-wrapper">
        <label for="category-filter-gantt"></label>
        <select id="category-filter-gantt" v-model="selectedCategory" @change="onSearch" class="category-filter">
          <option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option>
        </select>
      </div>
    </div>

    <!-- Gantt 컨테이너 -->
    <div ref="ganttContainer" class="gantt-container"></div>

    <!-- 피드백 마커 (학생 전용 표시) -->
    <FeedbackLayer
      v-if="!isReadOnly"
      :page="'task-board'"
      :readonly="true"
    />

    <!-- 피드백 마커 (교수 전용 표시) -->
    <div
      v-if="isReadOnly"
      v-for="fb in feedbacks"
      :key="fb.feedbackId"
      class="feedback-marker"
      :style="{ top: fb.y + 'px', left: fb.x + 'px', position: 'absolute' }"
      @click="selectedFeedback = fb"
    >
      📌
    </div>

    <!-- 피드백 팝업 -->
    <FeedbackPopup
      v-if="selectedFeedback"
      :fb="selectedFeedback"
      :readonly="true"
      @read="handleReadFeedback(selectedFeedback.feedbackId)"
      @close="selectedFeedback = null"
    />

    <!-- 피드백 입력창 (교수 전용) -->
    <FeedbackInput
      v-if="showFeedbackInput"
      :x="feedbackX"
      :y="feedbackY"
      :page="'schedule-view'"
      :readonly="true"
      :project-id="projectId"
      @close="showFeedbackInput = false"
      @submitted="() => { showFeedbackInput = false; loadFeedbacks(); }"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onActivated, onDeactivated, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import gantt from 'dhtmlx-gantt'

import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import FeedbackLayer from '@/components/feedback/FeedbackLayer.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'

const route = useRoute()
const isReadOnly = computed(() => route.query.readonly === 'true')
const routeProjectId = computed(() => Number(route.params.projectId))
const ganttContainer = ref(null)
const projectId = ref(routeProjectId.value || null)

const currentUser = ref('')
const rawTeamMembers = ref([])
const teamMembers = ref([])
const searchTerm = ref('')
const filterMode = ref('all')
const selectedCategory = ref('ALL');
const categories = {
  ALL: '전체',
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트',
};
let allRows = []

const ganttEventIds = [] // 이벤트 핸들러 ID 저장용 배열

// 피드백 상태
const showFeedbackInput = ref(false)
const feedbackX = ref(0)
const feedbackY = ref(0)
const feedbacks = ref([])
const selectedFeedback = ref(null)


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

async function fetchCurrentUser() {
  if (isReadOnly.value) return
  try {
    const { data } = await axios.get('/auth/me')
    currentUser.value = data.userName.trim()
  } catch (e) {
    console.error('유저 정보 로드 실패', e)
  }
}

async function fetchTeamMembers() {
  try {
    const { data } = await axios.get('/projects/members/students', { params: { projectId: projectId.value } })
    rawTeamMembers.value = data.filter(u => u.role === 'STUDENT')
    teamMembers.value = rawTeamMembers.value.map(u => ({ key: u.userName, label: u.userName, userId: u.userId, color: u.userColor }))
  } catch (e) {
    console.error('팀원 정보 가져오기 실패', e)
  }
}

function flattenTask(task, parent = null) {
  const start = new Date(task.startDate)
  const end = new Date(task.endDate)
  const duration = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  const member = rawTeamMembers.value.find(u => u.userName === task.assignedUserName)
  const row = [{
    id: task.id,
    text: task.title,
    start_date: task.startDate,
    duration,
    assignee: task.assignedUserName,
    color: member?.userColor || null,
    parent,
    status: task.status, // status 필드 추가
    category: task.category // category 필드 추가
  }]
  if (task.childTasks) task.childTasks.forEach(c => row.push(...flattenTask(c, task.id)))
  return row
}

async function fetchTasksFromServer() {
  try {
    const res = await axios.get('/work-tasks/project', { params: { projectId: projectId.value } })
    allRows = res.data.flatMap(t => flattenTask(t))
    onSearch()
  } catch (e) {
    console.error('작업 불러오기 실패', e)
  }
}

async function loadFeedbacks() {
  if (!projectId.value) return;
  try {
    const res = await axios.get('/feedbacks/project', {
      params: {
        page: 'schedule-view',
        projectId: projectId.value
      },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    // 학생에게는 안 읽은 피드백만, 교수에게는 모든 피드백을 보여줄 수 있습니다.
    // 여기서는 학생과 동일하게 안 읽은 피드백만 표시하도록 처리합니다.
    feedbacks.value = res.data.filter(fb => !fb.isRead)
  } catch (err) {
    console.error('❌ 피드백 불러오기 오류:', err)
  }
}

function handleReadFeedback(id) {
  feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== id)
  selectedFeedback.value = null
}

function renderGantt(rows) {
  gantt.clearAll()
  gantt.parse({ data: rows, links: [] })
  gantt.render()
}

function onSearch() {
  let rows = allRows
  const q = searchTerm.value.trim().toLowerCase()
  if (q) rows = rows.filter(r => r.text.toLowerCase().includes(q))
  if (filterMode.value === 'mine') rows = rows.filter(r => r.assignee?.trim() === currentUser.value)
  if (selectedCategory.value !== 'ALL') rows = rows.filter(r => r.category === selectedCategory.value);
  renderGantt(rows)
}

function setFilter(mode) {
  filterMode.value = mode
  onSearch()
}

function setupGantt() {
  gantt.plugins({ drag_timeline: true })
  gantt.locale.date.month_full = ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월']
  gantt.locale.date.month_short = [...gantt.locale.date.month_full]
  gantt.locale.labels.section_description = '작업 이름'
  gantt.locale.labels.section_time        = '기간'
  gantt.locale.labels.section_assignee    = '담당자'
  gantt.locale.labels.section_status      = '상태' // 상태 섹션 라벨 추가
  gantt.locale.labels.section_category    = '분류' // 카테고리 섹션 라벨 추가
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

  gantt.config.readonly      = isReadOnly.value
  gantt.config.drag_move     = !isReadOnly.value
  gantt.config.drag_resize   = !isReadOnly.value
  gantt.config.drag_progress = !isReadOnly.value

  gantt.config.columns = [
    { name:'text',       label:'작업',   tree:true, width:130, align:'center', resize:true },
    { name:'category',   label:'카테고리', width:70, align:'center', template: (task) => categories[task.category] || '' },
    { name:'start_date', label:'시작일', width:100, align:'center', resize:true },
    { name:'duration',   label:'기간(일)', width:60,  align:'center' },
    { name:'assignee',   label:'담당자', width:70,  align:'center', editor:'select', options:teamMembers.value },
    { name:'add',        label:'',       width:60,  template: () => isReadOnly.value ? '' : '<button onclick="gridAddTask()">+작업</button>', resize:false }
  ]

  gantt.config.lightbox.sections = [
    { name:'description', map_to:'text', type:'textarea', label:'작업 이름' },
    { name:'time',        map_to:'auto', type:'duration', label:'기간' },
    { name:'category',    map_to:'category', type:'select', options: [
        { key: 'PLANNING',    label: '기획' },
        { key: 'DESIGN',      label: '설계' },
        { key: 'DEVELOPMENT', label: '개발' },
        { key: 'TEST',        label: '테스트' }
    ], label:'카테고리' },
    // 상태 선택 드롭다운 추가
    { name:'status',      map_to:'status', type:'select', options: [
        { key: 'PENDING',     label: '작업 목록' },
        { key: 'IN_PROGRESS', label: '진행 중' },
        { key: 'COMPLETED',   label: '완료' }
    ], label:'상태' },
    { name:'assignee',    map_to:'assignee', type:'select', options:teamMembers.value, label:'담당자' }
  ]
}

function onRightClick(event) {
  if (!isReadOnly.value) return
  event.preventDefault()
  feedbackX.value = event.clientX
  feedbackY.value = event.clientY
  showFeedbackInput.value = true
}

async function loadAndInitializeGantt() {
  // 1. 필수 데이터 로드
  await fetchProjectInfo()
  await fetchCurrentUser()
  await fetchTeamMembers()

  // 2. Gantt 설정
  setupGantt()
  await nextTick()

  // 3. 이전 이벤트 핸들러 정리 (중복 방지)
  ganttEventIds.forEach(id => gantt.detachEvent(id));
  ganttEventIds.length = 0;
  if (ganttContainer.value && isReadOnly.value) {
    ganttContainer.value.removeEventListener('contextmenu', onRightClick);
  }

  // 4. 새 이벤트 핸들러 등록
  if (!isReadOnly.value) {
    ganttEventIds.push(gantt.attachEvent("onAfterTaskAdd", (tempId, task) => {
      const startStr = gantt.date.date_to_str("%Y-%m-%d")(task.start_date)
      const endStr   = gantt.date.date_to_str("%Y-%m-%d")(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration }))
      const sel      = rawTeamMembers.value.find(u => u.userName === task.assignee)
      if (sel?.userColor) task.color = sel.userColor
      const payload  = { title: task.text, startDate: startStr, endDate: endStr, assignedUserId: sel?.userId ?? null, status: 'PENDING', parentTaskId: task.parent || null, category: task.category }
      axios.post('/work-tasks', payload)
          .then(({ data }) => gantt.changeTaskId(tempId, data.id))
          .catch(err => { console.error('작업 생성 실패', err); gantt.deleteTask(tempId) })
    }));

    ganttEventIds.push(gantt.attachEvent("onAfterTaskDrag", (id, mode) => {
      if (mode !== 'move' && mode !== 'resize') return
      const task = gantt.getTask(id)
      const payload = {
        startDate: gantt.date.date_to_str("%Y-%m-%d")(task.start_date),
        endDate: gantt.date.date_to_str("%Y-%m-%d")(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration, task: task }))
      }
      axios.patch(`/work-tasks/${id}/schedule`, payload).catch(err => console.error('일정 업데이트 실패', err))
    }));

    ganttEventIds.push(gantt.attachEvent('onAfterTaskUpdate', (id, task) => {
      const startStr = gantt.date.date_to_str('%Y-%m-%d')(task.start_date)
      const endStr   = gantt.date.date_to_str('%Y-%m-%d')(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration }))
      const sel      = rawTeamMembers.value.find(u => u.userName === task.assignee)
      const dto = {
        title: task.text,
        startDate: startStr,
        endDate: endStr,
        assignedUserId: sel?.userId ?? null,
        status: task.status,
        parentTaskId: task.parent || null,
        category: task.category
      }
      axios.patch(`/work-tasks/${id}`, dto)
          .catch(err => console.error('작업 업데이트 실패', err))
    }));

    ganttEventIds.push(gantt.attachEvent("onBeforeTaskDelete", id => {
      axios.delete(`/work-tasks/${id}`).catch(err => { console.error('작업 삭제 실패', err); fetchTasksFromServer() })
      return true
    }));
  }

  // 5. Gantt 초기화 및 데이터 렌더링
  if (ganttContainer.value) {
    gantt.init(ganttContainer.value)
    await fetchTasksFromServer()
    await loadFeedbacks() // 피드백 로드 추가
    if (isReadOnly.value) {
      ganttContainer.value.addEventListener('contextmenu', onRightClick)
    }

    // URL 쿼리에서 카테고리 필터링
    if (route.query.category) {
      selectedCategory.value = route.query.category;
      onSearch();
    }
  }
}

function cleanupGantt() {
  // 등록된 이벤트 핸들러만 정리합니다.
  ganttEventIds.forEach(id => gantt.detachEvent(id));
  ganttEventIds.length = 0;

  // DOM에 직접 추가한 이벤트 리스너도 제거합니다.
  if (ganttContainer.value) {
    ganttContainer.value.removeEventListener('contextmenu', onRightClick);
  }
  // gantt.destructor()는 전역 gantt 객체를 파괴하므로 사용하지 않습니다.
  // 대신 clearAll()을 사용하여 차트 내용만 지웁니다.
  gantt.clearAll();
}

onMounted(loadAndInitializeGantt);

onBeforeUnmount(cleanupGantt);
onActivated(loadAndInitializeGantt);
onDeactivated(cleanupGantt);
</script>

<style scoped>
.task-board-page {
  padding: 24px;
  background-color: #f7f8fc;
  position: relative;
}

/* 검색창 및 필터 디자인 개선 */
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
  padding: 8px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}
.search-bar input {
  width: 220px;
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 14px;
}
.search-bar input:focus {
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}
.filter-toggle {
  margin-left: 12px;
}
.filter-toggle button {
  margin-left: 5px;
  padding: 8px 14px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}
.filter-toggle button.active {
  background: #3f8efc;
  color: #fff;
  border-color: #3f8efc;
}

.category-filter-wrapper {
  margin-left: 12px;
  display: flex;
  align-items: center;
  gap: 8px;
}
.category-filter {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #dee2e6;
}
/* 간트 차트 컨테이너 디자인 */
.gantt-container {
  width: 100%;
  height: calc(100vh - 160px);
  margin: 0;
  padding: 0;
  position: relative;
  overflow: visible;
  z-index: 0;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
}

.feedback-marker {
  font-size: 18px;
  cursor: pointer;
  position: absolute;
}

/* dhtmlx-gantt 내부 요소 디자인 오버라이드 */
:deep(.gantt_grid_scale .gantt_scale_cell),
:deep(.gantt_task_scale .gantt_scale_cell) {
  background-color: #f8f9fa;
  border-color: #e9ecef;
  color: #495057;
  font-weight: 600;
}

:deep(.gantt_row),
:deep(.gantt_task_row) {
  border-color: #f1f3f5 !important;
}

:deep(.gantt_grid_data .gantt_row.odd:not(.gantt_selected)),
:deep(.gantt_task_bg .gantt_task_row.odd) {
  background-color: #fdfdff;
}

:deep(.gantt_task_line) {
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  border: 1px solid rgba(0,0,0,0.1);
}

:deep(.gantt_task_line .gantt_task_progress) {
  background: linear-gradient(to right, #28a745, #218838);
  opacity: 0.8;
  border-radius: 4px;
}

:deep(.gantt_task_line.gantt_project) {
  height: 8px !important;
  margin-top: 4px !important;
  background: #6c757d;
}

/* 라이트박스(모달) 디자인 */
:deep(.gantt_cal_light) {
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  background-color: #f8f9fa;
}

:deep(.gantt_cal_ltitle) {
  border-bottom: 1px solid #e9ecef;
  color: #212529;
  font-weight: 600;
}

:deep(.gantt_cal_lsection) {
  color: #495057;
  font-weight: 500;
}

:deep(.gantt_cal_larea textarea),
:deep(.gantt_cal_larea select) {
  border: 1px solid #ced4da;
  border-radius: 6px;
  padding: 8px;
  transition: border-color .15s ease-in-out,box-shadow .15s ease-in-out;
}
:deep(.gantt_cal_larea textarea:focus),
:deep(.gantt_cal_larea select:focus) {
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}

:deep(.gantt_btn_set button) {
  border-radius: 6px;
  padding: 8px 16px;
  font-weight: 600;
  border: none;
  cursor: pointer;
}
:deep(.gantt_btn_set .gantt_save_btn) {
  background-color: #007bff;
  color: white;
}
:deep(.gantt_btn_set .gantt_cancel_btn) {
  background-color: #6c757d;
  color: white;
}
:deep(.gantt_btn_set .gantt_delete_btn) {
  background-color: #dc3545;
  color: white;
}
</style>