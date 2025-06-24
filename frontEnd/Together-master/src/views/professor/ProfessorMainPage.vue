<template>
  <div class="professor-mainpage">
    <h2 class="page-title">📂 담당 프로젝트 목록</h2>

    <!-- 🔘 연도 필터 버튼 -->
    <div class="year-buttons">
      <button
        :class="{ active: selectedYear === null }"
        @click="selectedYear = null"
      >
        전체보기
      </button>
      <button
        v-for="year in availableYears"
        :key="year"
        :class="{ active: selectedYear === year }"
        @click="selectedYear = year"
      >
        {{ year }}
      </button>
    </div>

    <!-- 📋 프로젝트 카드 목록 -->
    <div class="project-cards">
      <TeamCard
        v-for="project in filteredProjects"
        :key="project.projectId"
        :project="project"
        @viewProject="handleViewProject"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import TeamCard from '@/components/professor/TeamCard.vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const projects = ref([])
const selectedYear = ref(null)
const availableYears = ref([])

const filteredProjects = computed(() => {
  return selectedYear.value
    ? projects.value.filter((p) => {
        const date = p.createdAt ? new Date(p.createdAt) : null
        return date && date.getFullYear() === selectedYear.value
      })
    : projects.value
})

onMounted(async () => {
  try {
    const authHeader = localStorage.getItem('authHeader')
    if (!authHeader) {
      console.error('❌ Authorization 헤더가 없습니다.')
      return
    }

    // ✅ 교수 프로젝트 ID 리스트 가져오기
    const res = await axios.get('/auth/me', {
      headers: { Authorization: authHeader },
      withCredentials: true
    })

    const professorProjects = res.data.projectId
    console.log('📦 professorProjects:', professorProjects)

    const yearsSet = new Set()

    const detailedProjects = await Promise.all(
      professorProjects.map(async (p, i) => {
        console.log(`🔍 [${i}] projectId: ${p.projectId}, createdAt: ${p.createdAt}`)

        const projectId = p.projectId

        const [projectRes, planningRes, taskRes, memberRes, noticeRes] = await Promise.all([
          axios.get(`/projects/${projectId}`, {
            headers: { Authorization: authHeader },
            withCredentials: true
          }),
          axios.get(`/planning/all`, {
            params: { projectId },
            headers: { Authorization: authHeader },
            withCredentials: true
          }),
          axios.get(`/work-tasks/project`, {
            params: { projectId },
            headers: { Authorization: authHeader },
            withCredentials: true
          }),
          axios.get(`/projects/members`, {
            params: { projectId },
            headers: { Authorization: authHeader },
            withCredentials: true
          }),
          axios.get(`/notices/all-notice`, {
            params: { projectId },
            headers: { Authorization: authHeader },
            withCredentials: true
          })
        ])

        // ✅ createdAt 확인 및 연도 추출
        const createdAt = p.createdAt
        if (createdAt) {
          const parsedDate = new Date(createdAt)
          const year = parsedDate.getFullYear()
          console.log(`📆 ${i}번 프로젝트 연도 추출:`, year)
          if (!isNaN(year)) {
            yearsSet.add(year)
          }
        } else {
          console.warn(`⚠️ ${i}번 프로젝트에 createdAt이 없음`)
        }

        const tasks = taskRes.data || []
        const totalTasks = tasks.length
        const completedTasks = tasks.filter(t => t.status === 'COMPLETED').length
        const progress = totalTasks === 0 ? 0 : Math.round((completedTasks / totalTasks) * 100)

        const teamMembers = memberRes.data.map(member => ({
          name: member.userName,
          id: member.userId,
          role: member.role,
          avatarUrl: member.avatarUrl || ''
        }))

        return {
          ...projectRes.data,
          createdAt,
          description: planningRes.data.description?.text || '',
          progress,
          members: teamMembers,
          notices: noticeRes.data || []
        }
      })
    )

    availableYears.value = Array.from(yearsSet)
      .filter(y => typeof y === 'number' && !isNaN(y))
      .sort()

    console.log('📅 최종 availableYears:', availableYears.value)

    selectedYear.value = null
    projects.value = detailedProjects
  } catch (error) {
    console.error('❌ 교수 프로젝트 목록 조회 실패:', error)
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

.year-buttons {
  margin-bottom: 24px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}
.year-buttons button {
  padding: 6px 14px;
  border: 1px solid #ccc;
  background: white;
  cursor: pointer;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
}
.year-buttons button.active {
  background: #2c7be5;
  color: white;
  border-color: #2c7be5;
}

.project-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
</style>
