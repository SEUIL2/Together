// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../views/MainPage.vue'
import MainPage2 from '../views/MainPage2.vue'
import Signup from '../views/account/Signup.vue'
import Login from '../views/account/Login.vue'
import CreateProject from '../views/CreateProject.vue'
const routes = [
  {
    path: '/',
    name: 'MainPage',
    component: MainPage,
  },
  {
    path: '/MainPage2',
    name: 'MainPage2',
    component: MainPage2,
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
  {
    path: '/CreateProject',
    name: 'CreateProject',
    component: CreateProject,
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
