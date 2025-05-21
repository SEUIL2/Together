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
import TeamManagement from "@/views/TeamManagement.vue"
import EmailVerification from "@/views/account/EmailVerificationPage.vue"
import SignupDetails from "@/views/account/SignupDetailsPage.vue"
import ErdDiagramPage from '../views/ErdDiagramPage.vue'
import TaskPage from '../views/TaskPage.vue'
import ProfessorMainPage from '../views/professor/ProfessorMainPage.vue'
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
  {
    path: '/EmailVerification',
    name: 'EmailVerification',
    component: EmailVerification,
  },
  {
    path: '/SignupDetails',
    name: 'SignupDetails',
    component: SignupDetails,
  },
  {
    path: '/ErdDiagramPage',
    name: 'ErdDiagramPage',
    component: ErdDiagramPage,
  },
  {
    path: '/TaskPage',
    name: 'TaskPage',
    component: TaskPage,
  },
  {
  path: '/professor/MainPage',
  component: ProfessorMainPage,
  meta: { requiresAuth: true, role: 'PROFESSOR' }
},
{
    path: '/professor/project/:projectId',
    name: 'ProfessorReadOnlyProject',
    component: MyProject,
    props: route => ({
      projectId: Number(route.params.projectId),
      readonly: route.query.readonly === 'true',
      projectTitle: route.query.projectTitle || ''
    }),
    meta: { requiresAuth: true, role: 'PROFESSOR' }
  },
  {
    path: '/professor/dashboard/:projectId',
    name: 'ProfessorDashboard',
    component: DashBoard,
    props: route => ({
      projectId: Number(route.params.projectId),
      readonly: route.query.readonly === 'true',
      projectTitle: route.query.projectTitle || ''
    }),
    meta: { requiresAuth: true, role: 'PROFESSOR' }
  },
  {
  path: '/professor/task/:projectId',
  name: 'ProfessorTaskPage',
  component: TaskPage,
  props: route => ({
    projectId: Number(route.params.projectId),
    readonly: route.query.readonly === 'true',
    projectTitle: route.query.projectTitle || ''
  }),
  meta: { requiresAuth: true, role: 'PROFESSOR' }
},
{
  path: '/professor/schedule/:projectId',
  name: 'ProfessorSchedulePage',
  component: Scheduletest,
  props: route => ({
    projectId: Number(route.params.projectId),
    readonly: route.query.readonly === 'true',
    projectTitle: route.query.projectTitle || ''
  }),
  meta: { requiresAuth: true, role: 'PROFESSOR' }
},
{
  path: '/professor/team/:projectId',
  name: 'ProfessorTeamManagement',
  component: TeamManagement,
  props: route => ({
    projectId: Number(route.params.projectId),
    readonly: route.query.readonly === 'true',
    projectTitle: route.query.projectTitle || ''
  }),
  meta: { requiresAuth: true, role: 'PROFESSOR' }
},
{
  path: '/professor/meeting/:projectId',
  name: 'ProfessorMeetingPage',
  component: MeetingPage,
  props: route => ({
    projectId: Number(route.params.projectId),
    readonly: route.query.readonly === 'true',
    projectTitle: route.query.projectTitle || ''
  }),
  meta: { requiresAuth: true, role: 'PROFESSOR' }
}


  
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
