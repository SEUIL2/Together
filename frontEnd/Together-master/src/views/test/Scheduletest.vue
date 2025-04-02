<template>
  <div>
    <!-- 저장/불러오기 버튼 영역 -->
    <div style="margin-bottom: 10px;">
      <button @click="saveData">저장하기</button>
      <button @click="triggerFileInput">불러오기</button>
      <!-- 파일 선택을 위한 숨겨진 input -->
      <input type="file" ref="fileInput" style="display: none" accept=".json" @change="handleFileUpload" />
    </div>
    <!-- 전체 화면을 차지하는 Gantt 컨테이너 -->
    <div ref="ganttContainer" style="width: 100%; height: calc(100vh - 50px);"></div>
  </div>
</template>

<script setup>
import {ref, onMounted, nextTick} from 'vue'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import gantt from 'dhtmlx-gantt'

// 예시 작업 데이터
let tasks = [
  {id: 1, text: "환경 세팅", start_date: "2025-01-10", duration: 10},
  {id: 2, text: "ER 다이어그램", start_date: "2025-01-20", duration: 5}
]

// dhtmlx-gantt 데이터 구조
const ganttData = {data: tasks, links: []}

// DOM 요소 참조
const ganttContainer = ref(null)
const fileInput = ref(null)

// 새 작업 생성 함수
function createTask() {
  const newId = Date.now()
  tasks.push({
    id: newId,
    text: "새 작업 " + newId,
    start_date: "2025-02-01",
    duration: 7
  })
  // 차트 갱신
  gantt.clearAll()
  ganttData.data = tasks.slice()
  gantt.parse(ganttData)
}

// 그리드 헤더의 버튼에서도 호출할 수 있도록 전역 함수 등록
window.gridAddTask = createTask

// 저장 함수: 현재 ganttData를 JSON 파일로 저장합니다.
function saveData() {
  const dataToSave = JSON.stringify(ganttData, null, 2)
  const blob = new Blob([dataToSave], {type: "application/json"})
  const url = URL.createObjectURL(blob)
  const a = document.createElement("a")
  a.href = url
  a.download = "gantt-data.json"
  a.click()
  URL.revokeObjectURL(url)
}

// 파일 입력을 트리거하는 함수 (불러오기 버튼 클릭 시 실행)
function triggerFileInput() {
  fileInput.value.click()
}

// 파일 업로드 핸들러: 파일을 읽고 ganttData를 업데이트합니다.
function handleFileUpload(e) {
  const file = e.target.files[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = (event) => {
    try {
      const importedData = JSON.parse(event.target.result);
      // 불러온 데이터의 날짜 필드를 Date 객체로 변환
      importedData.data = importedData.data.map(task => {
        task.start_date = new Date(task.start_date);
        task.end_date = new Date(task.end_date);
        return task;
      });
      tasks = importedData.data;
      ganttData.links = importedData.links;
      gantt.clearAll();
      ganttData.data = tasks.slice();
      gantt.parse(ganttData);
    } catch (error) {
      console.error("파일 불러오기 실패:", error);
    }
  };
  reader.readAsText(file);
}



onMounted(() => {
  // 스케일 및 날짜 설정
  gantt.config.scale_unit = "month"
  gantt.config.step = 1
  gantt.config.date_scale = "%M"
  gantt.config.subscales = [
    {unit: "day", step: 1, date: "%j"}
  ]
  gantt.config.drag_resize = true
  gantt.config.drag_move = true
  gantt.config.drag_progress = true
  gantt.config.date_format = "%Y-%m-%dT%H:%i:%s.%uZ";



  // 기본 내장 작업 추가 버튼 비활성화
  gantt.config.show_add_task_button = false

  // 사용자 정의 그리드 컬럼 설정 (마지막 컬럼에 "상위 작업 생성" 버튼 추가)
  gantt.config.columns = [
    {name: "text", label: "작업", width: "*", tree: true},
    {name: "start_date", label: "시작일", align: "center", width: 100},
    {name: "duration", label: "기간", align: "center", width: 80},
    {name: "assignee", label: "담당자", align: "center", width: 120, editor: "text"},
    {
      name: "add",
      label: "",
      width: 100,
      header: "<button class='grid-add-btn' onclick='gridAddTask()'>상위 작업 생성</button>",
      template: () => ""
    }
  ]

  // 툴팁 설정 (한글)
  gantt.templates.tooltip_text = function (start, end, task) {
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

  // DOM 업데이트 후 재렌더링
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
