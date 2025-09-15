<template>
  <div class="professor-mainpage">
    <h2 class="page-title">📂 담당 프로젝트 목록</h2>

    <!-- 🔘 연도 필터 드롭다운 -->
    <div class="year-filter">
      <div class="current-year-display">
        <span class="current-year-label">{{ selectedYear }}년 프로젝트</span>
        <button class="dropdown-toggle" @click="toggleYearDropdown">
          연도 선택
          <span class="chevron" :class="{ open: showYearDropdown }">▾</span>
        </button>
      </div>
      <div v-if="showYearDropdown" class="year-dropdown">
        <button
            v-for="year in availableYears"
            :key="year"
            :class="{ active: selectedYear === year }"
            @click="selectYear(year)"
        >
          {{ year }}
        </button>
      </div>
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
const currentYear = new Date().getFullYear()
const selectedYear = ref(currentYear)
const availableYears = ref([])
const showYearDropdown = ref(false)

const extractYear = (value) => {
  if (!value && value !== 0) {
    return null
  }

  if (typeof value === 'number' && Number.isFinite(value)) {
    return Math.trunc(value)
  }

  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return value.getFullYear()
  }

  if (typeof value === 'string') {
    const trimmed = value.trim()
    if (!trimmed) {
      return null
    }

    const directNumber = Number(trimmed)
    if (!Number.isNaN(directNumber) && trimmed.length === 4) {
      return Math.trunc(directNumber)
    }

    const parsedDate = new Date(trimmed)
    if (!Number.isNaN(parsedDate.getTime())) {
      return parsedDate.getFullYear()
    }

    const yearMatch = trimmed.match(/\d{4}/)
    if (yearMatch) {
      const matchedYear = Number(yearMatch[0])
      if (!Number.isNaN(matchedYear)) {
        return Math.trunc(matchedYear)
      }
    }
  }

  return null
}

const buildYearRange = (startYear, endYear) => {
  const safeStart = Math.min(startYear, endYear)
  const years = []

  for (let year = safeStart; year <= endYear; year += 1) {
    years.push(year)
  }

  if (years.length === 0) {
    years.push(endYear)
  }

  return Array.from(new Set(years)).sort((a, b) => a - b)
}

const selectYear = (year) => {
  selectedYear.value = year
  showYearDropdown.value = false
}

const toggleYearDropdown = () => {
  showYearDropdown.value = !showYearDropdown.value
}

const filteredProjects = computed(() => {
  return projects.value.filter((project) => {
    if (!selectedYear.value) {
      return true
    }

    const projectYear = project.createdYear ?? extractYear(
        project.createdAt || project.createdDate || project.startDate || project.updatedAt
    )

    if (projectYear == null) {
      return selectedYear.value === currentYear
    }

    return projectYear === selectedYear.value
  })
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

    const professorProjectsRaw = res.data.projectId
    const professorProjects = Array.isArray(professorProjectsRaw)
        ? professorProjectsRaw
        : professorProjectsRaw
            ? [professorProjectsRaw]
            : []
    console.log('📦 professorProjects:', professorProjects)

    const joinYearCandidates = [
      res.data.joinYear,
      res.data.joinDate,
      res.data.joinedAt,
      res.data.createdAt,
      res.data.createdDate,
      res.data.createdDateTime,
      res.data.registrationDate,
      res.data.signupDate
    ]

    const detectedJoinYear = joinYearCandidates
        .map(candidate => extractYear(candidate))
        .find(year => typeof year === 'number' && !Number.isNaN(year))

    const joinYear = detectedJoinYear ?? currentYear

    availableYears.value = buildYearRange(joinYear, currentYear)
    if (!availableYears.value.includes(currentYear)) {
      availableYears.value.push(currentYear)
      availableYears.value.sort((a, b) => a - b)
    }
    selectedYear.value = currentYear

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

          // ✅ [수정] 프로젝트 상세 정보에서 정확한 createdAt 값을 사용합니다.
          const createdAt = projectRes.data.createdAt
          const projectYear = extractYear(createdAt)
          if (projectYear != null) {
            console.log(`📆 ${i}번 프로젝트 연도 추출:`, projectYear)
            yearsSet.add(projectYear)
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
            createdYear: projectYear,
            description: planningRes.data.description?.text || '',
            progress,
            members: teamMembers,
            notices: noticeRes.data || []
          }
        })
    )

    const discoveredYears = Array.from(yearsSet)
        .filter(y => typeof y === 'number' && !Number.isNaN(y))
        .sort((a, b) => a - b)
    console.log('📅 프로젝트에서 확인된 연도:', discoveredYears)
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

.year-filter {
  margin-bottom: 24px;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  position: relative;
}

.current-year-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.current-year-label {
  font-size: 16px;
  font-weight: 600;
  color: #222;
}

.dropdown-toggle {
  padding: 6px 14px;
  border: 1px solid #ccc;
  background: white;
  cursor: pointer;
  border-radius: 6px;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 6px;
}

.dropdown-toggle:hover {
  border-color: #2c7be5;
  color: #2c7be5;
}

.chevron {
  transition: transform 0.2s ease;
}

.chevron.open {
  transform: rotate(180deg);
}

.year-dropdown {
  position: absolute;
  top: 42px;
  left: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.12);
  z-index: 10;
  min-width: 140px;
}

.year-dropdown button {
  padding: 6px 10px;
  border: 1px solid transparent;
  background: none;
  text-align: left;
  border-radius: 6px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  transition: all 0.2s ease;
}

.year-dropdown button:hover {
  background: #f3f4f6;
}

.year-dropdown button.active {
  background: #2c7be5;
  color: white;
}

.project-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
</style>