// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../views/MainPage.vue'

const routes = [
  {
    path: '/',
    name: 'MainPage',
    component: MainPage,
  },
  // 다른 라우트들이 있다면 여기에 추가
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
