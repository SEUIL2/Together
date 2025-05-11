<template>
  <div class="task-board-page">
    <!-- 검색 + 필터 -->
    <div class="search-bar">
      <input
        v-model="searchTerm"
        @input="onSearch"
        type="text"
        placeholder="작업 검색..."
      />
      <div class="filter-toggle">
        <button :class="{ active: filterMode === 'all' }" @click="setFilter('all')">모든 작업</button>
        <button :class="{ active: filterMode === 'mine' }" @click="setFilter('mine')">내 작업</button>
      </div>
    </div>

    <hr class="divider" />

    <div class="board">
      <!-- 작업 목록 -->
      <div class="column">
        <h3><span>작업 목록</span></h3>
        <draggable v-model="pendingList" :group="{ name: 'tasks' }" @change="evt => onTaskDrop(evt, 'PENDING')" class="task-list">
          <template #item="{ element: task }">
            <div class="card" @dblclick="openLightbox(task.id)">
              <div class="card-title">{{ task.title }}</div>
              <div class="card-assignee">{{ task.assignedUserName }}</div>
            </div>
          </template>
        </draggable>
      </div>

      <!-- 진행 중 -->
      <div class="column">
        <h3><span>진행 중</span></h3>
        <draggable v-model="inProgressList" :group="{ name: 'tasks' }" @change="evt => onTaskDrop(evt, 'IN_PROGRESS')" class="task-list">
          <template #item="{ element: task }">
            <div class="card" @dblclick="openLightbox(task.id)">
              <div class="card-title">{{ task.title }}</div>
              <div class="card-assignee">{{ task.assignedUserName }}</div>
            </div>
          </template>
        </draggable>
      </div>

      <!-- 완료 -->
      <div class="column">
        <h3><span>완료</span></h3>
        <draggable v-model="completedList" :group="{ name: 'tasks' }" @change="evt => onTaskDrop(evt, 'COMPLETED')" class="task-list">
          <template #item="{ element: task }">
            <div class="card" @dblclick="openLightbox(task.id)">
              <div class="card-title">{{ task.title }}</div>
              <div class="card-assignee">{{ task.assignedUserName }}</div>
            </div>
          </template>
        </draggable>
      </div>
    </div>

    <div ref="ganttHidden" style="width:0;height:0;overflow:hidden;"></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import draggable from 'vuedraggable'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import gantt from 'dhtmlx-gantt'

axios.defaults.baseURL = 'http://localhost:8081'
axios.defaults.withCredentials = true

const searchTerm = ref('')
const filterMode = ref('all')
const currentUser = ref('')
const ganttHidden = ref(null)

const workTasks = ref([])
const pendingList = ref([])
const inProgressList = ref([])
const completedList = ref([])

const rawTeamMembers = ref([])
const teamMembers = ref([])

async function fetchCurrentUser() {
  const { data } = await axios.get('/auth/me')
  currentUser.value = data.userName?.trim()
}

async function fetchTeamMembers() {
  const { data } = await axios.get('/projects/members')
  rawTeamMembers.value = data
  teamMembers.value = data.map(u => ({
    key: u.userName,
    label: u.userName,
    userId: u.userId
  }))
}

function splitByStatus(filteredTasks) {
  pendingList.value    = filteredTasks.filter(t => t.status === 'PENDING')
  inProgressList.value = filteredTasks.filter(t => t.status === 'IN_PROGRESS')
  completedList.value  = filteredTasks.filter(t => t.status === 'COMPLETED')
}

async function fetchTasks() {
  const { data } = await axios.get('/work-tasks/project')
  workTasks.value = data
  onSearch()
}

function onSearch() {
  const q = searchTerm.value.trim().toLowerCase()
  let filtered = workTasks.value

  if (q) {
    filtered = filtered.filter(t => t.title.toLowerCase().includes(q))
  }

  if (filterMode.value === 'mine') {
    filtered = filtered.filter(t =>
      t.assignedUserName && currentUser.value &&
      t.assignedUserName.trim() === currentUser.value.trim()
    )
  }

  splitByStatus(filtered)
}

function setFilter(mode) {
  filterMode.value = mode
  onSearch()
}

function flattenTask(t, parent = null) {
  const start = new Date(t.startDate)
  const end   = new Date(t.endDate)
  const dur   = Math.ceil((end - start) / (1000*60*60*24))
  const row = [{
    id:         t.id,
    text:       t.title,
    start_date: t.startDate,
    duration:   dur,
    assignee:   t.assignedUserName,
    parent
  }]
  if (t.childTasks) {
    t.childTasks.forEach(c => row.push(...flattenTask(c, t.id)))
  }
  return row
}

function refreshHiddenGantt() {
  const rows = workTasks.value.flatMap(t => flattenTask(t))
  gantt.clearAll()
  gantt.parse({ data: rows, links: [] })
}

async function onTaskDrop(evt, newStatus) {
  if (!evt.added) return
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
  } finally {
    await fetchTasks()
    refreshHiddenGantt()
  }
}

function openLightbox(id) {
  refreshHiddenGantt()
  gantt.showLightbox(id)
}

onMounted(async () => {
  await fetchCurrentUser()
  await fetchTeamMembers()
  await fetchTasks()

  gantt.init(ganttHidden.value)

  gantt.locale.labels.section_description = '작업 이름'
  gantt.locale.labels.section_time = '기간'
  gantt.locale.labels.section_assignee = '담당자'
  gantt.locale.labels.button_save = '저장'
  gantt.locale.labels.button_cancel = '취소'
  gantt.locale.labels.button_delete = '삭제'

  gantt.config.lightbox.sections = [
    { name:'description', map_to:'text',     type:'textarea', label:'작업 이름' },
    { name:'time',        map_to:'auto',     type:'duration', label:'기간' },
    { name:'assignee',    map_to:'assignee', type:'select',   options: teamMembers.value, label:'담당자' }
  ]

  gantt.attachEvent('onAfterTaskUpdate', async (id, task) => {
    const startStr = gantt.date.date_to_str('%Y-%m-%d')(task.start_date)
    const endStr   = gantt.date.date_to_str('%Y-%m-%d')(
      gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration })
    )
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
    } finally {
      await fetchTasks()
      refreshHiddenGantt()
    }
  })

  gantt.attachEvent('onBeforeTaskDelete', async id => {
    try {
      await axios.delete(`/work-tasks/${id}`)
    } finally {
      await fetchTasks()
      refreshHiddenGantt()
    }
    return true
  })

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
  height: 550px;
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
</style>
