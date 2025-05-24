<template>
  <div class="professor-mainpage">
    <h2 class="page-title">📂 담당 프로젝트 목록</h2>

    <!-- 프로젝트 카드 목록 -->
    <div class="project-cards">
      <TeamCard
        v-for="project in projects"
        :key="project.projectId"
        :project="project"
        @viewProject="handleViewProject"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import TeamCard from '@/components/professor/TeamCard.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const projects = ref([])

onMounted(async () => {
  try {
    const authHeader = localStorage.getItem('authHeader')
    if (authHeader) {
      axios.defaults.headers.common['Authorization'] = authHeader
    }

    // 교수 프로젝트 ID 리스트 가져오기
    const res = await axios.get('/auth/me')
    const professorProjects = res.data.projectId

    // 각 프로젝트 정보 + 설명 병합해서 가져오기
const detailedProjects = await Promise.all(
  professorProjects.map(async (p) => {
    const [projectRes, planningRes, taskRes, memberRes] = await Promise.all([
      axios.get(`/projects/${p.projectId}`),
      axios.get(`/planning/all?projectId=${p.projectId}`),
      axios.get(`/work-tasks/project`, {
        params: { projectId: p.projectId },
        headers: { Authorization: authHeader },
        withCredentials: true
      }),
      axios.get(`/projects/members`, {
        params: { projectId: p.projectId },
        headers: { Authorization: authHeader },
        withCredentials: true
      })
    ])

    const tasks = taskRes.data || []
    const totalTasks = tasks.length
    const completedTasks = tasks.filter(t => t.status === 'COMPLETED').length
    const progress = totalTasks === 0 ? 0 : Math.round((completedTasks / totalTasks) * 100)

    const teamMembers = memberRes.data.map(member => ({
      name: member.userName,
      id: member.userId
    }))

    return {
      ...projectRes.data,
      description: planningRes.data.description?.text || '',
      progress,
      members: teamMembers
    }
  })
)



    projects.value = detailedProjects
  } catch (error) {
    console.error('교수 프로젝트 목록 조회 실패:', error)
  }
})

function handleViewProject(projectId) {
  const selectedProject = projects.value.find(p => p.projectId === projectId)
  const projectTitle = selectedProject?.title || '프로젝트'
  router.push(`/professor/project/${projectId}?readonly=true&projectTitle=${encodeURIComponent(projectTitle)}`)
}
</script>

<style scoped>
.professor-mainpage {
  padding: 30px;
  max-width: 1200px;
  margin: 0 auto;
}
.page-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #222;
}

.project-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
</style>
