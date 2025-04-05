import { createApp } from 'vue'
import App from './App.vue'
import router from './router'

// ✅ 인증 쿠키 포함 설정
import axios from 'axios'
axios.defaults.withCredentials = true

import { GcSpreadSheets, GcWorksheet, GcColumn } from '@grapecity/spread-sheets-vue'
import './api'

// Gantt 차트
import GanttChart from 'vue-ganttastic'

// Konva
import VueKonva from 'vue-konva'

// 마크다운 에디터 관련
import VMdEditor from '@kangc/v-md-editor'
import '@kangc/v-md-editor/lib/style/base-editor.css'
import vuepressTheme from '@kangc/v-md-editor/lib/theme/vuepress.js'
import '@kangc/v-md-editor/lib/theme/style/vuepress.css'
import Prism from 'prismjs'
import koKR from '@kangc/v-md-editor/lib/lang/ko-KR'

VMdEditor.lang.use('ko-KR', koKR)
VMdEditor.use(vuepressTheme, { Prism })

const app = createApp(App)

app.component('gc-spread-sheets', GcSpreadSheets)
app.component('gc-worksheet', GcWorksheet)
app.component('gc-column', GcColumn)

app.component('GanttChart', GanttChart)
app.use(VueKonva)
app.use(VMdEditor)

app.use(router)
app.mount('#app')
