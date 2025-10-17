<template>
  <div class="similarity-check-container">
    <h2>프로젝트 유사성 검사</h2>
    <div class="controls">
      <select v-model="selectedProject" class="project-select">
        <option disabled value="">프로젝트를 선택하세요</option>
        <option v-for="project in projects" :key="project.projectId" :value="project.projectId">
          {{ project.projectTitle }}
        </option>
      </select>
      <button @click="startCheck" :disabled="!selectedProject || isLoading" class="start-button">
        {{ isLoading ? '검사 중...' : '검사 시작' }}
      </button>
    </div>
    <div class="chart-container" v-if="chartData">
      <Bar :data="chartData" :options="chartOptions" />
    </div>
    <div v-if="!chartData && !isLoading" class="placeholder">
      프로젝트를 선택하고 검사 시작 버튼을 눌러주세요.
    </div>
    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import apiInstance from '@/api';
import { Bar } from 'vue-chartjs';
import { Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale } from 'chart.js';

ChartJS.register(Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale);

export default {
  name: 'ProjectSimilarityCheck',
  components: { Bar },
  setup() {
    const projects = ref([]);
    const selectedProject = ref('');
    const isLoading = ref(false);
    const similarityReport = ref(null);
    const error = ref(null);

    const fetchProjects = async () => {
      try {
        const response = await apiInstance.get('/api/projects/professor');
        projects.value = response.data;
      } catch (err) {
        console.error('프로젝트 목록을 불러오는 데 실패했습니다.', err);
        error.value = '프로젝트 목록을 불러오는 데 실패했습니다.';
      }
    };

    onMounted(fetchProjects);

    const startCheck = async () => {
      if (!selectedProject.value) {
        alert('프로젝트를 선택해주세요.');
        return;
      }
      isLoading.value = true;
      similarityReport.value = null;
      error.value = null;
      try {
        const response = await apiInstance.post(`/api/project-similarity/report/${selectedProject.value}`);
        similarityReport.value = response.data;
      } catch (err) {
        console.error('유사성 검사에 실패했습니다.', err);
        error.value = '유사성 검사에 실패했습니다. 유사성 검사는 프로젝트 당 하루에 한 번만 가능합니다.';
      } finally {
        isLoading.value = false;
      }
    };

    const chartData = computed(() => {
      if (!similarityReport.value || !similarityReport.value.comparisonResults) {
        return null;
      }
      const labels = similarityReport.value.comparisonResults.map(r => r.comparedProjectTitle);
      const data = similarityReport.value.comparisonResults.map(r => r.similarityScore * 100); // 백분율로 표시

      return {
        labels,
        datasets: [
          {
            label: '유사도 (%)',
            backgroundColor: '#42A5F5',
            data,
          },
        ],
      };
    });

    const chartOptions = ref({
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: true,
          position: 'top',
        },
        title: {
          display: true,
          text: '프로젝트 유사성 검사 결과',
        },
      },
      scales: {
        y: {
          beginAtZero: true,
          max: 100, // y축 최대값을 100으로 설정
          ticks: {
            callback: function(value) {
              return value + "%"
            }
          }
        },
      },
    });

    return {
      projects,
      selectedProject,
      isLoading,
      startCheck,
      chartData,
      chartOptions,
      error
    };
  },
};
</script>

<style scoped>
.similarity-check-container {
  padding: 2rem;
  background-color: #f9f9f9;
  border-radius: 8px;
  max-width: 900px;
  margin: 2rem auto;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 2rem;
  color: #333;
}

.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 2rem;
  gap: 1rem;
}

.project-select {
  padding: 0.5rem 1rem;
  border-radius: 4px;
  border: 1px solid #ccc;
  min-width: 300px;
}

.start-button {
  padding: 0.5rem 1.5rem;
  border: none;
  background-color: #007bff;
  color: white;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
}

.start-button:hover {
  background-color: #0056b3;
}

.start-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.chart-container {
  position: relative;
  height: 400px; /* 고정 높이 */
  width: 100%; /* 너비는 부모에 맞춤 */
}


.placeholder {
  text-align: center;
  color: #888;
  margin-top: 4rem;
  font-size: 1.2rem;
}

.error-message {
  text-align: center;
  color: red;
  margin-top: 1rem;
}
</style>