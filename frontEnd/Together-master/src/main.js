import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia' // ✅ Pinia import
import { API_BASE_URL } from './config'
// ✅ api 인스턴스를 가져와서 사용합니다.
import api from './api'

async function fetchCsrfToken() {
    try {
        // 이 GET 호출이 CsrfFilter를 타며 Set-Cookie 헤더를 내려줍니다
        // API_BASE_URL 대신 api 인스턴스의 baseURL('/api')이 사용됩니다.
        await api.get(`/csrf`)
    } catch (e) {
        console.error('CSRF 토큰 받아오기 실패', e)
    }
}

import { GcSpreadSheets, GcWorksheet, GcColumn } from '@grapecity/spread-sheets-vue'

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
const pinia = createPinia()        // ✅ Pinia 인스턴스 생성
app.use(pinia)

app.component('gc-spread-sheets', GcSpreadSheets)
app.component('gc-worksheet', GcWorksheet)
app.component('gc-column', GcColumn)

app.component('GanttChart', GanttChart)
app.use(VueKonva)
app.use(VMdEditor)

app.use(router)
app.mount('#app')
