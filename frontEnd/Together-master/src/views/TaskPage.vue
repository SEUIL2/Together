<template>
  <div class="task-board-page" @contextmenu.prevent="handleRightClick" style="position: relative">
    <!-- 검색 + 필터 -->
    <div class="search-bar">
      <input v-model="searchTerm" @input="onSearch" type="text" placeholder="작업 검색..." />
      <div class="filter-toggle">
        <button :class="{ active: filterMode === 'all' }" @click="setFilter('all')">모든 작업</button>
        <button :class="{ active: filterMode === 'mine' }" @click="setFilter('mine')">내 작업</button>
      </div>
    </div>

    <hr class="divider" />

    <div class="board">
      <!-- 작업 컬럼들 -->
      <div class="column" v-for="(tasks, status) in taskColumns" :key="status">
        <h3><span>{{ columnTitles[status] }}</span></h3>
        <draggable
          v-model="taskColumns[status]"
          itemKey="id"
          :group="{ name: 'tasks' }"
          :disabled="isReadOnly"
          @change="evt => onTaskDrop(evt, status)"
          class="task-list"
        >
          <template #item="{ element: task }">
            <div class="card" @dblclick="openLightbox(task.id)">
              <div class="card-title">{{ task.title }}</div>
              <div class="card-assignee">{{ task.assignedUserName }}</div>
            </div>
          </template>
        </draggable>
      </div>

      <!-- 📌 피드백 마커 -->
      <div
        v-for="(fb, index) in feedbacks"
        :key="index"
        class="feedback-marker"
        :style="{ top: fb.y + 'px', left: fb.x + 'px', position: 'absolute', zIndex: 10 }"
        @click="selectedFeedback = fb"
      >
        📌
      </div>

      <!-- 피드백 팝업 -->
      <div style="position: absolute; z-index: 20">
        <FeedbackPopup
          v-if="selectedFeedback"
          :fb="selectedFeedback"
          :readonly="true"
          @read="handleReadFeedback"
          @close="selectedFeedback = null"
        />
      </div>

      <!-- 피드백 입력창 (교수 전용) -->
      <FeedbackInput
        v-if="showFeedbackInput"
        :x="feedbackPosition.x"
        :y="feedbackPosition.y"
        :page="'task-board'"
        :readonly="true"
        :projectId="Number(route.params.projectId)"
        @close="showFeedbackInput = false"
        @submitted="() => { showFeedbackInput = false; loadFeedbacks() }"
      />
    </div>

    <div ref="ganttHidden" style="width:0;height:0;overflow:hidden;"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import draggable from 'vuedraggable'
import gantt from 'dhtmlx-gantt'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import { useFeedback } from '@/composables/useFeedback'

const route = useRoute()
const isReadOnly = computed(() => route.query.readonly === 'true')
const ganttHidden = ref(null)

const searchTerm = ref('')
const filterMode = ref('all')
const currentUser = ref('')
const workTasks = ref([])
const rawTeamMembers = ref([])
const teamMembers = ref([])
const columnTitles = { PENDING: '작업 목록', IN_PROGRESS: '진행 중', COMPLETED: '완료' }
const taskColumns = ref({ PENDING: [], IN_PROGRESS: [], COMPLETED: [] })

const feedbacks = ref([])
const showFeedbackInput = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const selectedFeedback = ref(null)
const { markFeedbackAsRead } = useFeedback()

function handleRightClick(e) {
  if (!isReadOnly.value) return
  const sectionRect = e.currentTarget.getBoundingClientRect()
  feedbackPosition.value = {
    x: e.clientX - sectionRect.left + e.currentTarget.scrollLeft,
    y: e.clientY - sectionRect.top + e.currentTarget.scrollTop
  }
  showFeedbackInput.value = true
}

async function loadFeedbacks() {
  try {
    const res = await axios.get('/feedbacks/project', {
      params: {
        page: 'task-board',
        projectId: route.params.projectId
      },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    feedbacks.value = res.data.filter(fb => !fb.isRead)
  } catch (err) {
    console.error('❌ 피드백 불러오기 오류:', err)
  }
}

function handleReadFeedback(id) {
  markFeedbackAsRead(id)
  feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== id)
  selectedFeedback.value = null
}

async function fetchCurrentUser() {
  if (isReadOnly.value) return
  try {
    const { data } = await axios.get('/auth/me')
    currentUser.value = data.userName?.trim()
  } catch (e) {
    console.error('사용자 정보 조회 실패:', e)
  }
}

async function fetchTeamMembers() {
  try {
    const { data } = await axios.get('/projects/members', {
      params: { projectId: route.params.projectId }
    })
    rawTeamMembers.value = data
    teamMembers.value = data.map(u => ({
      key: u.userName,
      label: u.userName,
      userId: u.userId
    }))
  } catch (e) {
    console.error('팀원 정보 조회 실패:', e)
  }
}

async function fetchTasks() {
  try {
    const { data } = await axios.get('/work-tasks/project', {
      params: { projectId: route.params.projectId }
    })
    workTasks.value = data
    onSearch()
  } catch (e) {
    console.error('작업 목록 조회 실패:', e)
  }
}

function splitByStatus(filteredTasks) {
  taskColumns.value.PENDING = filteredTasks.filter(t => t.status === 'PENDING')
  taskColumns.value.IN_PROGRESS = filteredTasks.filter(t => t.status === 'IN_PROGRESS')
  taskColumns.value.COMPLETED = filteredTasks.filter(t => t.status === 'COMPLETED')
}

function onSearch() {
  const q = searchTerm.value.trim().toLowerCase()
  let filtered = workTasks.value
  if (q) filtered = filtered.filter(t => t.title.toLowerCase().includes(q))
  if (filterMode.value === 'mine') {
    filtered = filtered.filter(t => t.assignedUserName?.trim() === currentUser.value.trim())
  }
  splitByStatus(filtered)
}

function setFilter(mode) {
  filterMode.value = mode
  onSearch()
}

function flattenTask(t, parent = null) {
  const start = new Date(t.startDate)
  const end = new Date(t.endDate)
  const dur = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  const row = [{ id: t.id, text: t.title, start_date: t.startDate, duration: dur, assignee: t.assignedUserName, parent }]
  if (t.childTasks) t.childTasks.forEach(c => row.push(...flattenTask(c, t.id)))
  return row
}

function refreshHiddenGantt() {
  const rows = workTasks.value.flatMap(t => flattenTask(t))
  gantt.clearAll()
  gantt.parse({ data: rows, links: [] })
}

async function onTaskDrop(evt, newStatus) {
  if (isReadOnly.value || !evt.added) return
  const task = evt.added.element
  const payload = {
    title: task.title,
    description: task.description || '',
    startDate: task.startDate,
    endDate: task.endDate,
    assignedUserId: task.assignedUserId,
    status: newStatus,
    parentTaskId: task.parentTaskId
  }
  try {
    await axios.patch(`/work-tasks/${task.id}`, payload)
  } catch (e) {
    console.error('작업 상태 변경 실패:', e)
  } finally {
    await fetchTasks()
    refreshHiddenGantt()
  }
}

function openLightbox(id) {
  if (isReadOnly.value) return
  refreshHiddenGantt()
  gantt.showLightbox(id)
}

onMounted(async () => {
  await fetchCurrentUser()
  await fetchTeamMembers()
  await fetchTasks()
  await loadFeedbacks()
  gantt.init(ganttHidden.value)

  gantt.config.readonly = isReadOnly.value
  gantt.locale.labels.section_description = '작업 이름'
  gantt.locale.labels.section_time = '기간'
  gantt.locale.labels.section_assignee = '담당자'
  gantt.locale.labels.button_save = '저장'
  gantt.locale.labels.button_cancel = '취소'
  gantt.locale.labels.button_delete = '삭제'

  gantt.config.lightbox.sections = [
    { name:'description', map_to:'text', type:'textarea', label:'작업 이름' },
    { name:'time', map_to:'auto', type:'duration', label:'기간' },
    { name:'assignee', map_to:'assignee', type:'select', options: teamMembers.value, label:'담당자' }
  ]

  if (!isReadOnly.value) {
    gantt.attachEvent('onAfterTaskUpdate', async (id, task) => {
      const startStr = gantt.date.date_to_str('%Y-%m-%d')(task.start_date)
      const endStr = gantt.date.date_to_str('%Y-%m-%d')(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration }))
      const sel = rawTeamMembers.value.find(u => u.userName === task.assignee)
      const dto = {
        title: task.text,
        description: task.text,
        startDate: startStr,
        endDate: endStr,
        assignedUserId: sel?.userId ?? null,
        status: task.status,
        parentTaskId: task.parent || null
      }
      try {
        await axios.patch(`/work-tasks/${id}`, dto)
      } catch (e) {
        console.error('작업 업데이트 실패:', e)
      } finally {
        await fetchTasks()
        refreshHiddenGantt()
      }
    })

    gantt.attachEvent('onBeforeTaskDelete', async id => {
      try {
        await axios.delete(`/work-tasks/${id}`)
      } catch (e) {
        console.error('작업 삭제 실패:', e)
      } finally {
        await fetchTasks()
        refreshHiddenGantt()
      }
      return true
    })
  }

  refreshHiddenGantt()
})
</script>


<style scoped>
.task-board-page {
  padding: 32px;
}
.search-bar {
  display: flex;
  align-items: center;
  margin-bottom: 0px;
  margin-left: 90px;
}
.search-bar input {
  width: 200px;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}
.filter-toggle button {
  margin-left: 3px;
  padding: 4px 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}
.filter-toggle button.active {
  background: #3f8efc;
  color: #fff;
  border-color: #3f8efc;
}
.board {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  height: 100%;
  min-height: 550px;
}
.column {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: #f0f0f0;
  border-radius: 12px;
  padding: 16px;
  min-width: 200px;
  overflow-y: auto;
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.column h3 {
  text-align: center;
  margin-bottom: 12px;
}
.column h3 span {
  background: #4A90E2;
  color: white;
  border-radius: 999px;
  padding: 4px 30px;
  font-size: 1rem;
}
.task-list {
  flex: 1;
  min-height: 100px;
  padding: 8px;
}
.card {
  background: #fff;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: grab;
}
.card-title {
  font-size: 1rem;
  margin-bottom: 4px;
}
.card-assignee {
  font-size: 0.85rem;
  color: #555;
}
.divider {
  border: none;
  border-top: 1px solid #bbbbbb;
  width: 87%;
  margin: 12px auto;
}
.feedback-marker {
  font-size: 18px;
  cursor: pointer;
  position: absolute;
}
.popup-container {
  position: absolute;
  z-index: 9999; /* 마커보다 위로 */
  background: white;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
</style>
