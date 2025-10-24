<template>
  <div class="similarity-check-page">
    <h1 class="page-title">프로젝트 유사도 검사</h1>
    <div class="controls-container">
      <div class="project-selector">
        <label for="base-project">기준 프로젝트 선택:</label>
        <select id="base-project" v-model="baseProjectId" class="dropdown">
          <option disabled value="">프로젝트를 선택하세요</option>
          <option v-for="project in projects" :key="project.projectId" :value="project.projectId">
            {{ project.title }}
          </option>
        </select>
      </div>
      <button @click="runSimilarityCheck" :disabled="!baseProjectId || isLoading" class="run-button">
        <span v-if="isLoading">검사 중...</span>
        <span v-else>유사도 검사 시작</span>
      </button>
    </div>

    <div v-if="isLoading" class="loading-indicator">
      <p>AI가 프로젝트 데이터를 분석하고 있습니다. 잠시만 기다려주세요...</p>
    </div>

    <div v-if="error" class="error-message">
      <p>{{ error }}</p>
    </div>

    <div v-if="report && !isLoading" class="results-container">
      <h2 class="results-title">유사도 분석 결과</h2>
      <p class="base-project-info"><strong>기준 프로젝트:</strong> {{ baseProjectTitle }}</p>
      <div class="results-grid">
        <div v-for="result in report.comparisonResults" :key="result.targetProjectId" class="result-card">
          <h3 class="target-project-title">{{ getProjectTitle(result.targetProjectId) }}</h3>
          <div class="similarity-scores">
            <div class="score-item">
              <span class="score-label">주제</span>
              <div class="score-bar-container">
                <div class="score-bar topic" :style="{ width: result.topicSimilarity.score + '%' }"></div>
              </div>
              <span class="score-value">{{ result.topicSimilarity.score }}%</span>
            </div>
             <p class="score-reason"><strong>사유:</strong> {{ result.topicSimilarity.reason }}</p>

            <div class="score-item">
              <span class="score-label">기능</span>
              <div class="score-bar-container">
                <div class="score-bar function" :style="{ width: result.functionSimilarity.score + '%' }"></div>
              </div>
              <span class="score-value">{{ result.functionSimilarity.score }}%</span>
            </div>
            <p class="score-reason"><strong>사유:</strong> {{ result.functionSimilarity.reason }}</p>

            <div class="score-item">
              <span class="score-label">기술 스택</span>
              <div class="score-bar-container">
                <div class="score-bar tech" :style="{ width: result.techStackSimilarity.score + '%' }"></div>
              </div>
              <span class="score-value">{{ result.techStackSimilarity.score }}%</span>
            </div>
             <p class="score-reason"><strong>사유:</strong> {{ result.techStackSimilarity.reason }}</p>

            <div class="score-item">
              <span class="score-label">핵심 키워드</span>
              <div class="score-bar-container">
                <div class="score-bar keyword" :style="{ width: result.keywordSimilarity.score + '%' }"></div>
              </div>
              <span class="score-value">{{ result.keywordSimilarity.score }}%</span>
            </div>
            <p class="score-reason"><strong>사유:</strong> {{ result.keywordSimilarity.reason }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/api';

const projects = ref([]);
const baseProjectId = ref('');
const isLoading = ref(false);
const report = ref(null);
const error = ref('');

const fetchProjects = async () => {
  try {
    const response = await api.get('/projects/professor/list');
    projects.value = response.data;
  } catch (err) {
    error.value = '프로젝트 목록을 불러오는 데 실패했습니다.';
    console.error(err);
  }
};

onMounted(fetchProjects);

const baseProjectTitle = computed(() => {
  const selectedProject = projects.value.find(p => p.projectId === baseProjectId.value);
  return selectedProject ? selectedProject.title : '';
});

const getProjectTitle = (projectId) => {
    const project = projects.value.find(p => p.projectId === projectId);
    return project ? project.title : '알 수 없는 프로젝트';
};

const runSimilarityCheck = async () => {
  if (!baseProjectId.value) {
    error.value = '기준 프로젝트를 선택해주세요.';
    return;
  }

  isLoading.value = true;
  report.value = null;
  error.value = '';

  const targetProjectIds = projects.value
    .filter(p => p.projectId !== baseProjectId.value)
    .map(p => p.projectId);

  if (targetProjectIds.length === 0) {
    error.value = '비교할 다른 프로젝트가 없습니다.';
    isLoading.value = false;
    return;
  }

  const requestDto = {
    baseProjectId: baseProjectId.value,
    targetProjectIds: targetProjectIds,
  };

  try {
    const response = await api.post('/professor/project-similarity/compare', requestDto);
    report.value = response.data;
     if (!report.value.comparisonResults || report.value.comparisonResults.length === 0) {
        error.value = '유사도 분석 결과를 받지 못했습니다. AI 서비스에 문제가 있을 수 있습니다.';
    }
  } catch (err) {
    error.value = '유사도 검사 중 오류가 발생했습니다.';
    console.error(err);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.similarity-check-page {
  padding: 2rem;
  font-family: 'Noto Sans KR', sans-serif;
  background-color: #f9fafb;
  min-height: 100vh;
}

.page-title {
  font-size: 2rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 1.5rem;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 0.5rem;
}

.controls-container {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background-color: #ffffff;
  padding: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  margin-bottom: 2rem;
}

.project-selector {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.project-selector label {
  font-size: 1rem;
  font-weight: 500;
  color: #374151;
}

.dropdown {
  padding: 0.5rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 1rem;
  min-width: 250px;
}

.run-button {
  padding: 0.6rem 1.5rem;
  background-color: #3f8efc;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.run-button:hover {
  background-color: #2b79e8;
}

.run-button:disabled {
  background-color: #9ca3af;
  cursor: not-allowed;
}

.loading-indicator, .error-message {
  text-align: center;
  padding: 2rem;
  margin-top: 2rem;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.error-message {
  color: #ef4444;
  font-weight: 500;
}

.results-container {
  margin-top: 2rem;
}

.results-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
}
.base-project-info {
  font-size: 1.1rem;
  color: #4b5563;
  margin-bottom: 1.5rem;
}

.results-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 1.5rem;
}

.result-card {
  background-color: #ffffff;
  border-radius: 8px;
  padding: 1.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.target-project-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #111827;
  margin-bottom: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0.75rem;
}

.similarity-scores {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.score-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.score-label {
  font-weight: 500;
  color: #374151;
  width: 80px;
  text-align: right;
}

.score-bar-container {
  flex-grow: 1;
  height: 20px;
  background-color: #e5e7eb;
  border-radius: 10px;
  overflow: hidden;
}

.score-bar {
  height: 100%;
  border-radius: 10px;
  transition: width 0.5s ease-in-out;
}

.score-bar.topic { background-color: #34d399; }
.score-bar.function { background-color: #60a5fa; }
.score-bar.tech { background-color: #f59e0b; }
.score-bar.keyword { background-color: #a78bfa; }

.score-value {
  font-weight: 600;
  color: #1f2937;
  width: 40px;
}
.score-reason {
  font-size: 0.9rem;
  color: #6b7280;
  margin: -1rem 0 0 0;
  padding-left: 96px; /* align with score bars */
}
</style>