// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../views/MainPage.vue'
import Signup from '../views/account/Signup.vue'
import Login from '../views/account/Login.vue'
const routes = [
  {
    path: '/',
    name: 'MainPage',
    component: MainPage,
  },
  {
    path: '/Signup',
    name: 'Signup',
    component: Signup,
  },
  {
    path: '/Login',
    name: 'Login',
    component: Login,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
