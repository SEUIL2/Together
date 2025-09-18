<template>
  <div class="professor-mainpage">
    <h2 class="page-title">📂 담당 프로젝트 목록</h2>

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

// 선택된 연도에 따라 프로젝트를 필터링합니다.
const filteredProjects = computed(() => {
  return projects.value.filter(p => p.createdYear === selectedYear.value);
});


function selectYear(year) {
  selectedYear.value = year;
  showYearDropdown.value = false;
}

function toggleYearDropdown() {
  showYearDropdown.value = !showYearDropdown.value;
}

onMounted(async () => {
  try {
    const authHeader = localStorage.getItem('authHeader')
    if (!authHeader) {
      console.error('❌ Authorization 헤더가 없습니다.')
      return
    }

    // 1. 교수가 관리하는 모든 프로젝트의 기본 정보를 가져옵니다.
    const res = await axios.get('/projects/my-projects/sorted-by-created', {
      headers: { Authorization: authHeader },
      withCredentials: true,
    });

    const allProjects = res.data || [];
    const years = new Set();

    // 2. 각 프로젝트의 생성 연도를 추출하고, 전체 연도 목록을 만듭니다.
    const processedProjects = allProjects.map(p => {
      const createdYear = p.createdAt ? new Date(p.createdAt).getFullYear() : currentYear;
      years.add(createdYear);
      return { ...p, createdYear };
    });

    projects.value = processedProjects;
    availableYears.value = Array.from(years).sort((a, b) => b - a); // 최신 연도 순으로 정렬

    // 만약 프로젝트가 있는 연도 중에 올해가 없다면, 올해를 추가해줍니다.
    if (!availableYears.value.includes(currentYear)) {
      availableYears.value.push(currentYear);
      availableYears.value.sort((a, b) => b - a);
    }

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