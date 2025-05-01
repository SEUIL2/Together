<template>
  <div>
    <!-- 파일 불러오기 -->
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

// 팀원 조회
const teamMembers = ref([])
async function fetchTeamMembers() {
  try {
    const { data } = await axios.get('/projects/members', { withCredentials: true })
    teamMembers.value = data.map(m => ({ key: m.userName, label: m.userName }))
  } catch (e) {
    console.error('팀원 정보 가져오기 실패', e)
  }
}

// 파일 업로드 핸들러
function handleFileUpload(e) {
  // …기존 구현…
}

// 샘플 데이터
let tasks = []
const ganttData = { data: tasks, links: [] }
const ganttContainer = ref(null)

// 새 작업 생성
function createTask() {
  const id = Date.now()
  tasks.push({ id, text:`새 작업 ${id}`, start_date:'2025-02-01', duration:7 })
  gantt.clearAll()
  gantt.parse({ data: tasks, links: [] })
}
window.gridAddTask = createTask

onMounted(async () => {
  // 1) 팀원 로드
  await fetchTeamMembers()

  gantt.plugins({ drag_timeline: true });
  // 2) 로케일 설정
  gantt.locale.date.month_full  = ['1월','2월','3월','4월','5월','6월','7월','8월','9월','10월','11월','12월']
  gantt.locale.date.month_short = [...gantt.locale.date.month_full]
  gantt.locale.labels.section_description = '작업 이름'
  gantt.locale.labels.section_time        = '시작일'
  gantt.locale.labels.section_assignee    = '담당자'
  gantt.locale.labels.button_save         = '저장'
  gantt.locale.labels.button_cancel       = '취소'
  gantt.locale.labels.button_delete       = '삭제'

  // 3) Gantt 설정
  gantt.config.scale_unit      = 'month'
  gantt.config.step            = 1
  gantt.config.date_scale      = '%M'
  gantt.config.subscales       = [{ unit:'day', step:1, date:'%j' }]
  gantt.config.date_format     = '%Y-%m-%dT%H:%i:%s.%uZ'
  gantt.config.start_date      = new Date(2025,0,1)
  gantt.config.end_date        = new Date(2025,11,31)
  gantt.config.show_add_task_button = false
  gantt.config.drag_resize   = true
  gantt.config.drag_move     = true
  gantt.config.drag_progress = true

  // 4) 컬럼 정의
  gantt.config.columns = [
    { name:'text',       label:'작업',    width:150, align:'center', resize:true },
    { name:'start_date', label:'시작일',  width:120, align:'center', resize:true },
    { name:'duration',   label:'기간',    width:80,  align:'center' },
    {
      name:'assignee',
      label:'담당자',
      width:100,
      align:'center',
      editor:'select',
      options:teamMembers.value
    },
    {
      name:'add', label:'', width:60,
      template:() =>
        `<button class="grid-add-btn" onclick="gridAddTask()">+작업</button>`,
      resize:false
    }
  ]

  // 5) Lightbox 정의
  gantt.config.lightbox.sections = [
    { name:'description', map_to:'text', type:'textarea', label:'작업 이름' },
    { name:'time',        map_to:'auto', type:'duration', label:'기간'     },
    { name:'assignee',    map_to:'assignee', type:'select', options:teamMembers.value, label:'담당자' }
  ]

  // 6) Tooltip
  gantt.templates.tooltip_text = (s,e,task) =>
    `<b>${task.text}</b><br/>시작일: ${gantt.templates.format_date(s)}<br/>마감일: ${gantt.templates.format_date(e)}`

  // 7) 초기화 및 파싱
  gantt.init(ganttContainer.value)
  gantt.parse(ganttData)

  // 8) **Scroll 확장** 초기화 (마우스 드래그로 타임라인 Panning)
  //    index.html 에서 로드된 ext/dhtmlxgantt_scroll.js가 전역에 붙어 있습니다.
  gantt.ext.scroll.init(gantt)

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
  font-size: 0.9rem;
}
.grid-add-btn:hover {
  background: #45a049;
}
</style>
