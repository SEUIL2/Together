import { createApp } from 'vue'
import App from './App.vue'
import router from './router'  // 라우터 임포트
import { GcSpreadSheets, GcWorksheet, GcColumn } from '@grapecity/spread-sheets-vue'
import './api'

// vue-ganttastic의 GanttChart를 default import 합니다.
import GanttChart from 'vue-ganttastic'

// vue-konva 플러그인 import
import VueKonva from 'vue-konva'

// (선택사항) CSS 파일이 존재한다면 아래도 추가
// import 'vue-ganttastic/dist/스타일파일.css';

const app = createApp(App)

app.component('gc-spread-sheets', GcSpreadSheets)
app.component('gc-worksheet', GcWorksheet)
app.component('gc-column', GcColumn)
// GanttChart 전역 컴포넌트 등록 (템플릿에서 <GanttChart> 로 사용)
app.component('GanttChart', GanttChart)

// vue-konva 플러그인 등록
app.use(VueKonva)

app.use(router)
app.mount('#app')
