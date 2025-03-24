<template>
  <!-- 전체 화면을 차지하는 컨테이너 -->
  <div ref="ganttContainer" style="width: 100%; height: 100vh;"></div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import gantt from 'dhtmlx-gantt'

// 예시 작업 데이터
const tasks = [
  { id: 1, text: "환경 세팅", start_date: "2025-01-10", duration: 10 },
  { id: 2, text: "ER 다이어그램", start_date: "2025-01-20", duration: 5 }
]

// dhtmlx-gantt 데이터 구조
const ganttData = { data: tasks, links: [] }

// Gantt 차트를 표시할 DOM 요소 참조
const ganttContainer = ref(null)

// 새 작업 생성 함수
function createTask() {
  const newId = Date.now()
  tasks.push({
    id: newId,
    text: "새 작업 " + newId,
    start_date: "2025-02-01",
    duration: 7
  })
  // 차트를 갱신합니다.
  gantt.clearAll()
  ganttData.data = tasks.slice()
  gantt.parse(ganttData)
}

// 전역 함수 등록: 그리드 헤더 버튼에서 호출
window.gridAddTask = createTask

onMounted(() => {
  // 스케일 및 날짜 설정
  gantt.config.scale_unit = "month"
  gantt.config.step = 1
  gantt.config.date_scale = "%M" // 예: "1월", "2월" 등 (필요시 추가 커스터마이징)
  gantt.config.subscales = [
    { unit: "day", step: 1, date: "%j" } // 일 숫자만 표시
  ]
  gantt.config.drag_resize = true
  gantt.config.drag_move = true
  gantt.config.drag_progress = true

  // 기본 내장 작업 추가 버튼 비활성화
  gantt.config.show_add_task_button = false

  // 사용자 정의 그리드 컬럼 설정 (마지막 컬럼에 "상위 작업 생성" 버튼 추가)
  gantt.config.columns = [
    { name: "text", label: "작업", width: "*", tree: true },
    { name: "start_date", label: "시작일", align: "center", width: 100 },
    { name: "duration", label: "기간", align: "center", width: 80 },
    { name: "assignee", label: "담당자", align: "center", width: 120, editor: "text" },
    { 
      name: "add", 
      label: "", 
      width: 100, 
      header: "<button class='grid-add-btn' onclick='gridAddTask()'>상위 작업 생성</button>",
      template: () => ""
    }
  ]

 // 툴팁 설정 (한글)
 gantt.templates.tooltip_text = function(start, end, task) {
    return `<b>${task.text}</b><br/>
            시작일: ${gantt.templates.format_date(start)}<br/>
            마감일: ${gantt.templates.format_date(end)}<br/>
            담당자: ${task.assignee || '-'}`;
  }
  

  // 전체 차트 날짜 범위 설정 (2025년)
  gantt.config.start_date = new Date(2025, 0, 1)
  gantt.config.end_date = new Date(2025, 11, 31)

  // 차트 초기화 및 데이터 파싱
  gantt.init(ganttContainer.value)
  gantt.parse(ganttData)

  // DOM 업데이트 후 강제 재렌더링 (헤더가 제대로 적용되도록)
  nextTick(() => {
    gantt.render()
  })
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
