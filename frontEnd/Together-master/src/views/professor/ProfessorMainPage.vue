<template>
  <div class="professor-mainpage">
    <h2>담당 프로젝트 목록</h2>
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

const projects = ref([])
const router = useRouter()

onMounted(async () => {
  try {
    const authHeader = localStorage.getItem('authHeader')
    if (authHeader) {
      axios.defaults.headers.common['Authorization'] = authHeader
    }

    const res = await axios.get('/auth/me')
    const professorProjects = res.data.projectId

    const detailedProjects = await Promise.all(
      professorProjects.map(async (p) => {
        const detailRes = await axios.get(`/projects/${p.projectId}`)
        return detailRes.data
      })
    )

    projects.value = detailedProjects
  } catch (error) {
    console.error('교수 프로젝트 목록 조회 실패:', error)
  }
})


function handleViewProject(projectId) {
  router.push(`/professor/project/${projectId}?readonly=true`)

}
</script>

<style scoped>
.professor-mainpage {
  padding: 20px;
}
.project-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}
</style>