// router/index.js
import { createRouter, createWebHistory } from 'vue-router'
import MainPage from '../views/MainPage.vue'
import MainPage2 from '../views/MainPage2.vue'
import Signup from '../views/account/Signup.vue'
import Login from '../views/account/Login.vue'
import CreateProject from '../views/CreateProject.vue'
import MyProject from '../views/MyProject.vue'
import DashBoard from '../views/DashBoard.vue'
import Schedule from '../views/Schedule.vue'
import SignUpForm from '../views/test/SignupForm.vue'
import Scheduletest from '../views/test/Scheduletest.vue'
import DiagramPage from '../views/DiagramPage.vue'
import MeetingPage from '../views/MeetingPage.vue'
import TeamManagement from "@/views/TeamManagement.vue";
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
  {
    path: '/MyProject',
    name: 'MyProject',
    component: MyProject,
  },
  {
    path: '/DashBoard',
    name: 'DashBoard',
    component: DashBoard,
  },
  {
    path: '/Schedule',
    name: 'Schedule',
    component: Schedule,
  },
  {
    path: '/SignUpForm',
    name: 'SignUpForm',
    component: SignUpForm,
  },
  {
    path: '/Scheduletest',
    name: 'Scheduletest',
    component: Scheduletest,
  },
  {
    path: '/DiagramPage',
    name: 'DiagramPage',
    component: DiagramPage,
  },
  {
    path: '/MeetingPage',
    name: 'MeetingPage',
    component: MeetingPage,
  },
  {
    path: '/TeamManagement',
    name: 'TeamManagement',
    component: TeamManagement,
  },
  
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
