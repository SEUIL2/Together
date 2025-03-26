import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { GcSpreadSheets, GcWorksheet, GcColumn } from '@grapecity/spread-sheets-vue'
import './api'

// Gantt 차트
import GanttChart from 'vue-ganttastic'

// Konva
import VueKonva from 'vue-konva'

// 📌 마크다운 에디터 관련 추가
import VMdEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js'
import '@kangc/v-md-editor/lib/theme/style/vuepress.css'
import Prism from 'prismjs'
import koKR from '@kangc/v-md-editor/lib/lang/ko-KR'

VMdEditor.lang.use('ko-KR', koKR)

// 마크다운 에디터 테마 적용
VMdEditor.use(vuepressTheme, { Prism })

const app = createApp(App)

// SpreadSheets 컴포넌트 등록
app.component('gc-spread-sheets', GcSpreadSheets)
app.component('gc-worksheet', GcWorksheet)
app.component('gc-column', GcColumn)

// GanttChart 전역 등록
app.component('GanttChart', GanttChart)

// Konva 등록
app.use(VueKonva)

// ✅ 마크다운 에디터 등록
app.use(VMdEditor)

app.use(router)
app.mount('#app')
