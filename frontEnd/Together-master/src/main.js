import { createApp } from 'vue'
import App from './App.vue'
import router from './router'  // 라우터 임포트
import { GcSpreadSheets, GcWorksheet, GcColumn} from '@grapecity/spread-sheets-vue'
import './api';

const app = createApp(App)
app.component('gc-spread-sheets', GcSpreadSheets);
app.component('gc-worksheet', GcWorksheet);
app.component('gc-column', GcColumn);
app.use(router)  // 라우터 적용
app.mount('#app')