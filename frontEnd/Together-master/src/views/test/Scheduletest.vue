<template>
  <div>
    <!-- (선택) 파일 업로드 -->
    <div style="margin-bottom:10px;">
      <input
        type="file"
        ref="fileInput"
        style="display:none"
        accept=".json"
        @change="handleFileUpload"
      />
    </div>
    <!-- Gantt 컨테이너 -->
    <div ref="ganttContainer" style="width:100%; height:calc(100vh - 50px);"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import axios from 'axios'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import gantt from 'dhtmlx-gantt'

// ✅ axios 전역 설정
axios.defaults.baseURL = 'http://localhost:8081'
axios.defaults.withCredentials = true

const ganttContainer = ref(null)
const rawTeamMembers = ref([]) // { userId, userName }
const teamMembers = ref([])    // { key: userName, label: userName }

// ✅ 팀원 정보 불러오기
async function fetchTeamMembers() {
  try {
    const { data } = await axios.get('/projects/members')
    rawTeamMembers.value = data
    teamMembers.value = data.map(u => ({
      key: u.userName,
      label: u.userName,
      userId: u.userId
    }))
  } catch (e) {
    console.error('팀원 정보 가져오기 실패', e)
  }
}

// ✅ 트리 구조 flatten 처리
function flattenTask(task, parent = null) {
  const start = new Date(task.startDate)
  const end = new Date(task.endDate)
  const duration = Math.ceil((end - start) / (1000*60*60*24))
  const row = [{
    id: task.id,
    text: task.title,
    start_date: task.startDate,
    duration,
    assignee: task.assignedUserName,
    parent
  }]
  if (task.childTasks?.length) {
    task.childTasks.forEach(ch =>
      row.push(...flattenTask(ch, task.id))
    )
  }
  return row
}

// ✅ 서버에서 작업 목록 불러오기
async function fetchTasksFromServer() {
  try {
    const { data } = await axios.get('/work-tasks/project')
    const rows = data.flatMap(t => flattenTask(t))
    gantt.clearAll()
    gantt.parse({ data: rows, links: [] })
  } catch (e) {
    console.error('작업 불러오기 실패', e)
  }
}

// ✅ 작업 추가 버튼
function gridAddTask() {
  const defaultUser = teamMembers.value[0]?.key ?? ''
  gantt.addTask({
    text: '새 작업',
    start_date: '2025-02-01',
    duration: 7,
    assignee: defaultUser,
    parent: null
  })
}
window.gridAddTask = gridAddTask

// (옵션) 파일 업로드 핸들러
function handleFileUpload(e) {
  // 필요 시 구현
}

// ✅ Gantt 설정 분리
function setupGantt() {
  gantt.plugins({ drag_timeline: true })

  gantt.locale.date.month_full = ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월']
  gantt.locale.date.month_short = [...gantt.locale.date.month_full]
  gantt.locale.labels.section_description = '작업 이름'
  gantt.locale.labels.section_time = '기간'
  gantt.locale.labels.section_assignee = '담당자'
  gantt.locale.labels.button_save = '저장'
  gantt.locale.labels.button_cancel = '취소'
  gantt.locale.labels.button_delete = '삭제'

  gantt.config.scales = [
    { unit: 'month', step: 1, format: "%M" },
    { unit: 'day', step: 1, format: "%j" }
  ]
  gantt.config.start_date = new Date(2025, 0, 1)
  gantt.config.end_date = new Date(2025, 11, 31)
  gantt.config.date_format = "%Y-%m-%d"
  gantt.config.show_add_task_button = false
  gantt.config.drag_resize = true
  gantt.config.drag_move = true
  gantt.config.drag_progress = true

  gantt.config.columns = [
    {
      name: 'text',
      label: '작업',
      tree: true,
      width: 150,
      align: 'center',
      resize: true
    },
    { name: 'start_date', label: '시작일', width: 120, align: 'center', resize: true },
    { name: 'duration', label: '기간(일)', width: 80, align: 'center' },
    {
      name: 'assignee',
      label: '담당자',
      width: 100,
      align: 'center',
      editor: 'select',
      options: teamMembers.value
    },
    {
      name: 'add',
      label: '',
      width: 60,
      template: () => `<button onclick="gridAddTask()">+작업</button>`,
      resize: false
    }
  ]

  gantt.config.lightbox.sections = [
    { name: 'description', map_to: 'text', type: 'textarea', label: '작업 이름' },
    { name: 'time', map_to: 'auto', type: 'duration', label: '기간' },
    {
      name: 'assignee',
      map_to: 'assignee',
      type: 'select',
      options: teamMembers.value,
      label: '담당자'
    }
  ]

  gantt.templates.tooltip_text = (s, e, task) =>
    `<b>${task.text}</b><br/>시작일: ${gantt.templates.format_date(s)}<br/>마감일: ${gantt.templates.format_date(e)}`
}

// ✅ Gantt 이벤트 연결 및 초기화
onMounted(async () => {
  await fetchTeamMembers()  // 먼저 팀원 정보를 불러오고
  setupGantt()              // 그 후에 Gantt 설정

  // 작업 생성 이벤트
  gantt.attachEvent("onAfterTaskAdd", (tempId, task) => {
    const startStr = gantt.date.date_to_str("%Y-%m-%d")(task.start_date)
    const endStr = gantt.date.date_to_str("%Y-%m-%d")(
      gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration })
    )
    const sel = rawTeamMembers.value.find(u => u.userName === task.assignee)
    const payload = {
      title: task.text,
      description: task.text,
      startDate: startStr,
      endDate: endStr,
      assignedUserId: sel?.userId ?? null,
      status: 'PENDING',
      parentTaskId: task.parent || null
    }
    axios.post('/work-tasks', payload)
      .then(({ data }) => gantt.changeTaskId(tempId, data.id))
      .catch(err => {
        console.error('작업 생성 실패', err)
        gantt.deleteTask(tempId)
      })
  })

  // 작업 일정 드래그 시 저장
  gantt.attachEvent("onAfterTaskDrag", (id, mode) => {
    if (mode !== 'move' && mode !== 'resize') return
    const task = gantt.getTask(id)
    const payload = {
      startDate: gantt.date.date_to_str("%Y-%m-%d")(task.start_date),
      endDate: gantt.date.date_to_str("%Y-%m-%d")(
        gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration })
      )
    }
    axios.patch(`/work-tasks/${id}/schedule`, payload)
      .catch(err => console.error('일정 업데이트 실패', err))
  })

  // 삭제 시 반영
  gantt.attachEvent("onBeforeTaskDelete", (id) => {
    axios.delete(`/work-tasks/${id}`)
      .catch(err => {
        console.error('작업 삭제 실패', err)
        fetchTasksFromServer()
      })
    return true
  })

  // Gantt 초기화 및 데이터 로드
  gantt.init(ganttContainer.value)
  await fetchTasksFromServer()
  nextTick(() => gantt.render())
})
</script>

<style scoped>
.grid-add-btn {
  background: #4caf50;
  color: #fff;
  border: none;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 4px;
}
.grid-add-btn:hover {
  background: #45a049;
}
</style>
