<template>
  <div class="professor-mainpage">
    <h2 class="page-title">📂 담당 프로젝트 목록</h2>

    <div class="filters-container">
      <div class="year-filter">
        <div class="current-year-display">
          <span class="current-year-label">{{ displayYearLabel }}</span>
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

      <div class="language-search">
        <input
            type="text"
            v-model="searchQuery"
            placeholder="개발 언어로 검색..."
            @keyup.enter="performSearch"
            class="search-input"
        />
        <button @click="performSearch" class="search-btn">검색</button>
        <button @click="resetSearch" class="reset-btn">초기화</button>
      </div>
    </div>


    <div class="project-cards">
      <TeamCard
          v-for="project in filteredProjects"
          :key="project.projectId"
          :project="project"
          @viewProject="handleViewProject"
          @createFeedback="handleCreateFeedback"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import api from '@/api'
import TeamCard from '@/components/professor/TeamCard.vue'
import { useRouter } from 'vue-router'
import { useGlobalToast } from '@/composables/useGlobalToast';

const router = useRouter()
const projects = ref([])
const originalProjects = ref([]); // ⭐️ 전체 프로젝트 목록 저장
const searchQuery = ref(''); // ⭐️ 검색어
const { showToast } = useGlobalToast();
const currentYear = new Date().getFullYear()
const selectedYear = ref(currentYear)
const availableYears = ref([])
const showYearDropdown = ref(false)

const filteredProjects = computed(() => {
  if (selectedYear.value === '전체') {
    return projects.value;
  }
  return projects.value.filter(p => p.createdYear === selectedYear.value);
});

const displayYearLabel = computed(() => {
  return selectedYear.value === '전체' ? '모든 프로젝트' : `${selectedYear.value}년 프로젝트`;
});

function selectYear(year) {
  selectedYear.value = year;
  showYearDropdown.value = false;
}

function toggleYearDropdown() {
  showYearDropdown.value = !showYearDropdown.value;
}

// ⭐️ 프로젝트 목록을 처리하는 공통 함수
async function processProjects(baseProjects) {
  const authHeader = localStorage.getItem('authHeader');
  const years = new Set();
  const detailedProjectsPromises = baseProjects.map(async (p) => {
    try {
      const [planningRes, tasksRes] = await Promise.all([
        api.get('/planning/all', { params: { projectId: p.projectId }, headers: { Authorization: authHeader }, withCredentials: true }),
        api.get('/work-tasks/project', { params: { projectId: p.projectId }, headers: { Authorization: authHeader }, withCredentials: true })
      ]);

      const description = planningRes.data.description?.text || '';
      const totalTasks = tasksRes.data.length;
      const completedTasks = tasksRes.data.filter(t => t.status === 'COMPLETED').length;
      const progress = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
      const createdYear = p.createdAt ? new Date(p.createdAt).getFullYear() : currentYear;
      years.add(createdYear);

      return { ...p, description, progress, createdYear };
    } catch (error) {
      console.error(`Project ID ${p.projectId}의 상세 정보 로딩 실패:`, error);
      const createdYear = p.createdAt ? new Date(p.createdAt).getFullYear() : currentYear;
      years.add(createdYear);
      return { ...p, description: '', progress: 0, createdYear };
    }
  });

  const processed = await Promise.all(detailedProjectsPromises);

  // 연도 목록 업데이트 (중복 방지)
  const sortedYears = Array.from(years).sort((a, b) => b - a);
  if (!sortedYears.includes(currentYear)) {
    sortedYears.unshift(currentYear);
  }

  // 기존 availableYears에 없는 연도만 추가
  const newYears = sortedYears.filter(y => !availableYears.value.includes(y));
  if (newYears.length > 0) {
    availableYears.value.push(...newYears);
    availableYears.value.sort((a, b) => (a === '전체' ? -1 : b === '전체' ? 1 : b - a));
  }

  return processed;
}


// ⭐️ 초기 프로젝트 목록을 불러오는 함수
async function fetchInitialProjects() {
  try {
    const authHeader = localStorage.getItem('authHeader');
    if (!authHeader) {
      console.error('❌ Authorization 헤더가 없습니다.');
      return;
    }
    const res = await api.get('/projects/my-projects/sorted-by-created', {
      headers: { Authorization: authHeader },
      withCredentials: true,
    });
    const baseProjects = res.data || [];
    projects.value = await processProjects(baseProjects);
    originalProjects.value = [...projects.value];

    // 최초 로딩 시 연도 목록 설정
    const years = new Set(projects.value.map(p => p.createdYear));
    const sortedYears = Array.from(years).sort((a, b) => b - a);
    if (!sortedYears.includes(currentYear)) {
      sortedYears.unshift(currentYear);
    }
    availableYears.value = ['전체', ...sortedYears];

  } catch (error) {
    console.error('❌ 교수 프로젝트 목록 조회 실패:', error);
  }
}

// ⭐️ 개발 언어로 프로젝트를 검색하는 함수
async function performSearch() {
  if (!searchQuery.value.trim()) {
    showToast('검색어를 입력해주세요.', 'warning');
    return;
  }
  try {
    const authHeader = localStorage.getItem('authHeader');
    const res = await api.get('/projects/search/language', {
      params: { lang: searchQuery.value },
      headers: { Authorization: authHeader },
      withCredentials: true,
    });
    const baseProjects = res.data || [];
    if (baseProjects.length === 0) {
      showToast('검색 결과가 없습니다.', 'info');
    }
    projects.value = await processProjects(baseProjects);
  } catch (error) {
    console.error('❌ 언어 검색 실패:', error);
    showToast('검색 중 오류가 발생했습니다.', 'error');
  }
}

// ⭐️ 검색 필터를 초기화하고 전체 목록을 보여주는 함수
function resetSearch() {
  searchQuery.value = '';
  projects.value = [...originalProjects.value];
  selectedYear.value = currentYear;
}


onMounted(fetchInitialProjects);

function handleViewProject(projectId) {
  const selectedProject = projects.value.find(p => p.projectId === projectId)
  const projectTitle = selectedProject?.title || '프로젝트'
  showToast('💡 우클릭으로 피드백을 남길 수 있습니다.');
  router.push(`/professor/project/${projectId}?readonly=true&projectTitle=${encodeURIComponent(projectTitle)}`)
}

function handleCreateFeedback(projectId) {
  const selectedProject = projects.value.find(p => p.projectId === projectId)
  const projectTitle = selectedProject?.title || '프로젝트'
  showToast('💡 우클릭으로 피드백을 남길 수 있습니다.');
  // 'step' 쿼리 파라미터를 추가하여 피드백 탭으로 바로 이동
  router.push(`/professor/project/${projectId}?readonly=true&projectTitle=${encodeURIComponent(projectTitle)}&step=피드백`)
}
</script>

<style scoped>
.professor-mainpage {
  padding: 30px;
  max-width: 100%;
  margin: 0 auto;
  background-color: #f7f8fc;
  min-height: 100vh;
}
.page-title {
  font-size: 20px;
  margin-bottom: 20px;
  color: #222;
}

.filters-container {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.year-filter {
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
  padding: 8px 14px;
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

.language-search {
  display: flex;
  align-items: center;
  gap: 8px;
}

.search-input {
  padding: 8px 12px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 14px;
  min-width: 200px;
}
.search-input:focus {
  outline: none;
  border-color: #2c7be5;
  box-shadow: 0 0 0 2px rgba(44, 123, 229, 0.2);
}

.search-btn, .reset-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  transition: background-color 0.2s ease;
  font-weight: 500;
}

.search-btn {
  background-color: #2c7be5;
}
.search-btn:hover {
  background-color: #1a64c7;
}

.reset-btn {
  background-color: #6c757d;
}
.reset-btn:hover {
  background-color: #5a6268;
}

.project-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 24px;
}
</style>