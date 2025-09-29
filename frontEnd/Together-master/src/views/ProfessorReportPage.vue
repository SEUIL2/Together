<template>
  <div class="report-page-container">
    <!-- 왼쪽: 프로젝트 및 보고서 목록 -->
    <aside class="report-sidebar">
      <div class="sidebar-header">
        <!-- 프로젝트 선택 드롭다운 -->
        <div class="project-selector">
          <label for="project-select">프로젝트 선택</label>
          <select id="project-select" v-model="selectedProjectId" @change="onProjectChange">
            <option :value="null" disabled>프로젝트를 선택하세요</option>
            <option v-for="project in projects" :key="project.projectId" :value="project.projectId">
              {{ project.title }}
            </option>
          </select>
        </div>
        <!-- 보고서 카테고리 필터 -->
        <div class="sidebar-title-area">
          <h3>보고서 목록</h3>
          <select v-model="selectedCategoryFilter" class="category-filter">
            <option v-for="(label, key) in filterCategories" :key="key" :value="key">{{ label }}</option>
          </select>
        </div>
        <button
          class="export-all-btn"
          @click="exportAllReportsAsPdf"
          :disabled="!selectedProjectId || reports.length === 0"
        >
          보고서 전체 PDF 추출
        </button>
      </div>
      <ul class="report-list" v-if="selectedProjectId">
        <li
          v-for="(report, index) in filteredReports"
          :key="report.id"
          :class="{ active: selectedIndex === index }"
          @click="selectReport(index)"
        >
          <div class="report-list-content">
            <span class="report-list-title">{{ report.title }}</span>
            <div class="report-list-meta">
              <span class="report-list-date">{{ formatDate(report.createdAt || new Date()) }}</span>
              <span v-if="report.category" class="category-badge-list" :class="`category-${report.category}`">{{ categoryLabels[report.category] }}</span>
            </div>
          </div>
        </li>
      </ul>
      <div v-if="selectedProjectId && filteredReports.length === 0" class="empty-list-message">
        해당 프로젝트에<br>보고서가 없습니다.
      </div>
      <div v-if="!selectedProjectId" class="empty-list-message">
        프로젝트를 먼저<br>선택해주세요.
      </div>
    </aside>

    <!-- 오른쪽: 보고서 상세 내용 -->
    <main class="report-content">
      <!-- 보고서 조회 -->
      <div v-if="selectedReport" class="report-view">
        <div class="view-header">
          <div class="view-header-main">
            <div class="view-header-left">
              <div class="title-wrapper">
                <h2>{{ selectedReport.title }}</h2>
                <span class="category-badge" :class="`category-${selectedReport.category}`">{{ categoryLabels[selectedReport.category] || '미지정' }}</span>
              </div>
              <span class="team-info"><strong>팀원:</strong> {{ selectedReport.teamInfo }}</span>
            </div>
            <div class="view-header-right">
              <button class="pdf-btn" @click="exportReportAsPdf">PDF 추출</button>
            </div>
          </div>
          <div class="header-meta">
            <span><strong>기간:</strong> {{ selectedReport.period }}</span>
          </div>
        </div>
        <div class="view-section">
          <h4>금주 진행 내용</h4>
          <p>{{ selectedReport.weeklyProgress }}</p>
        </div>
        <div class="view-section">
          <h4>문제점 및 해결 방안</h4>
          <p>{{ selectedReport.problemsAndSolutions || '내용 없음' }}</p>
        </div>
        <div class="view-section">
          <h4>향후 계획</h4>
          <p>{{ selectedReport.futurePlans }}</p>
        </div>

        <!-- 피드백 컴포넌트 -->
        <ReportFeedback
          :report-id="selectedReport.id"
          :is-professor="true"
          @feedback-updated="onFeedbackUpdated"
          class="feedback-wrapper"
        />
      </div>

      <!-- 초기 화면 -->
      <div v-else class="empty-state">
        <div class="empty-inner">
          <span class="empty-icon">📄</span>
          <p>왼쪽에서 프로젝트를 선택한 후<br>보고서를 확인하세요.</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import axios from '@/utils/axiosInstance.js'
import ReportFeedback from '@/components/ReportFeedback.vue';

const projects = ref([]);
const selectedProjectId = ref(null);
const reports = ref([]);
const selectedIndex = ref(null);

const categoryLabels = {
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트',
};
const filterCategories = {
  ALL: '전체',
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트',
};

const selectedCategoryFilter = ref('ALL');

const filteredReports = computed(() => {
  if (selectedCategoryFilter.value === 'ALL') {
    return reports.value;
  }
  return reports.value.filter(report => report.category === selectedCategoryFilter.value);
});

watch(selectedCategoryFilter, () => {
  selectedIndex.value = filteredReports.value.length > 0 ? 0 : null;
});

const selectedReport = computed(() => (selectedIndex.value !== null && filteredReports.value.length > selectedIndex.value ? filteredReports.value[selectedIndex.value] : null));

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

const fetchProjects = async () => {
  try {
    const { data } = await axios.get('/projects/my-projects/sorted-by-created');
    projects.value = data;
  } catch (error) {
    console.error('프로젝트 목록 로딩 실패:', error);
  }
};

const fetchReports = async () => {
  if (!selectedProjectId.value) return;
  try {
    const { data } = await axios.get('/reports', {
      params: { projectId: selectedProjectId.value }
    });
    if (Array.isArray(data)) {
      reports.value = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else {
      reports.value = [];
    }
    selectedIndex.value = reports.value.length > 0 ? 0 : null;
  } catch (error) {
    console.error('보고서 목록 로딩 실패:', error);
    reports.value = [];
    selectedIndex.value = null;
  }
};

onMounted(fetchProjects);

const onProjectChange = () => {
  reports.value = [];
  selectedIndex.value = null;
  fetchReports();
};

const onFeedbackUpdated = () => {
  // 피드백이 업데이트되면 보고서 목록을 다시 불러와서 화면을 갱신합니다.
  fetchReports();
};

const selectReport = (index) => {
  selectedIndex.value = index;
};

const exportReportAsPdf = async () => {
  if (!selectedReport.value) return;
  try {
    const response = await axios.get(
      `/reports/export/${selectedReport.value.id}`,
      { responseType: 'blob' }
    );
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    const fileName = `${selectedReport.value.title.replace(/\s/g, '_')}.pdf`;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('PDF 추출 실패:', error);
    alert('PDF를 추출하는 중 오류가 발생했습니다.');
  }
};

const exportAllReportsAsPdf = async () => {
  if (!selectedProjectId.value) {
    alert('프로젝트를 먼저 선택해주세요.');
    return;
  }
  if (reports.value.length === 0) {
    alert('추출할 보고서가 없습니다.');
    return;
  }

  try {
    const response = await axios.get(
      `/reports/export/all`,
      {
        params: { projectId: selectedProjectId.value },
        responseType: 'blob'
      }
    );
    const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
    const link = document.createElement('a');
    link.href = url;
    const selectedProject = projects.value.find(p => p.projectId === selectedProjectId.value);
    const fileName = `${selectedProject?.title || 'Project'}_전체_보고서.pdf`;
    link.setAttribute('download', fileName);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('전체 보고서 PDF 추출 실패:', error);
    alert('전체 보고서를 PDF로 추출하는 중 오류가 발생했습니다.');
  }

};
</script>

<style scoped>
.report-page-container {
  display: flex;
  height: calc(100vh - 61px); /* 헤더 높이 제외 */
  background: #f8f9fa;
}

/* --- 사이드바 --- */
.report-sidebar {
  width: 320px;
  background: #fff;
  border-right: 1px solid #e9ecef;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding-bottom: 1rem;
  margin-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.project-selector {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.project-selector label {
  font-size: 1.1rem;
  color: #343a40;
  font-weight: 600;
}
.project-selector select {
  width: 100%;
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #ccc;
  font-size: 15px;
}
.sidebar-title-area {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.sidebar-title-area h3 {
  font-size: 1.1rem;
  color: #343a40;
  margin: 0;
}
.category-filter {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
  width: 40%;
}
.export-all-btn {
  width: 100%;
  padding: 10px;
  background: #6c757d;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}
.export-all-btn:hover:not(:disabled) {
  background-color: #5a6268;
}
.export-all-btn:disabled {
  background-color: #ced4da;
  cursor: not-allowed;
}

.report-list {
  list-style: none;
  padding: 0;
  overflow-y: auto;
  flex-grow: 1;
}
.report-list li {
  padding: 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
  margin-bottom: 4px;
}
.report-list li:hover { background: #f1f3f5; }
.report-list li.active { background: #eef6ff; color: #3f8efc; font-weight: 600; }
.report-list-content { display: flex; flex-direction: column; gap: 4px; width: 100%; }
.report-list-title {
  display: block;
  font-size: 15px;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  flex-grow: 1;
}
.report-list-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.report-list-date {
  font-size: 12px;
  color: #868e96;
}
.category-badge-list {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
  color: white;
  font-weight: 600;
  line-height: 1.4;
}
.empty-list-message {
  text-align: center;
  color: #adb5bd;
  padding: 2rem 0;
  font-size: 14px;
  line-height: 1.5;
}

/* --- 콘텐츠 영역 --- */
.report-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: #f8f9fa;
}

/* 보고서 조회 스타일 */
.report-view { background: #fff; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.view-header {
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 1rem;
  margin-bottom: 1.5rem;
}
.view-header-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
}
.view-header-left {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}
.team-info {
  font-size: 14px;
  color: #495057;
}
.view-header-right { display: flex; gap: 0.75rem; }
.pdf-btn {
  background: #f8f9fa;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 13px;
  cursor: pointer;
  font-weight: 500;
}
.pdf-btn:hover { background-color: #f1f3f5; }

.view-header h2 { font-size: 24px; font-weight: 700; margin: 0; }
.header-meta { display: flex; align-items: center; gap: 1rem; color: #495057; margin-top: 0.75rem; }
.category-badge { display: inline-block; padding: 4px 10px; font-size: 12px; font-weight: 700; border-radius: 12px; color: #fff; }
.category-PLANNING { background-color: #ffaeae; }
.category-DESIGN { background-color: #f39c12; }
.category-DEVELOPMENT { background-color: #2ecc71; }
.category-TEST { background-color: #9b59b6; }
.view-section { margin-bottom: 1.5rem; }
.view-section h4 { font-size: 16px; font-weight: 600; color: #343a40; margin-bottom: 0.5rem; }
.view-section p { font-size: 15px; color: #495057; line-height: 1.7; white-space: pre-wrap; }
.feedback-wrapper {
  margin-top: 2rem;
}

/* 초기 화면 스타일 */
.empty-state { display: flex; justify-content: center; align-items: center; height: 100%; }
.empty-inner { text-align: center; color: #adb5bd; }
.empty-icon { font-size: 3rem; display: block; margin-bottom: 1rem; }
.empty-inner p { font-size: 1.1rem; line-height: 1.6; }
</style>