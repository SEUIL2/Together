<template>
  <div class="report-page-container">
    <!-- 왼쪽: 보고서 목록 -->
    <aside class="report-sidebar">
      <div class="sidebar-header">
        <h3>보고서 목록</h3>
        <button class="add-btn" @click="startNewReport">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="12" y1="5" x2="12" y2="19"></line><line x1="5" y1="12" x2="19" y2="12"></line></svg>
          <span>새 보고서 작성</span>
        </button>
      </div>
      <ul class="report-list">
        <li
          v-for="(report, index) in reports"
          :key="report.id"
          :class="{ active: selectedIndex === index }"
          @click="selectReport(index)"
        >
          <span class="report-list-title">{{ report.title }}</span>
          <span class="report-list-date">{{ formatDate(report.createdAt || new Date()) }}</span>
        </li>
      </ul>
    </aside>

    <!-- 오른쪽: 보고서 상세 내용 또는 작성 폼 -->
    <main class="report-content">
      <!-- 보고서 조회 -->
      <div v-if="!isCreatingNew && selectedReport" class="report-view">
        <div class="view-header">
          <h2>{{ selectedReport.title }}</h2>
          <div class="header-meta">
            <span><strong>기간:</strong> {{ selectedReport.reportingPeriod }}</span>
            <span class="category-badge" :class="`category-${selectedReport.category}`">{{ selectedReport.category }}</span>
          </div>
        </div>
        <div class="view-section">
          <h4>금주 진행 내용</h4>
          <p>{{ selectedReport.progressContent }}</p>
        </div>
        <div class="view-section">
          <h4>문제점 및 해결 방안</h4>
          <p>{{ selectedReport.problemsAndSolutions || '내용 없음' }}</p>
        </div>
        <div class="view-section">
          <h4>향후 계획</h4>
          <p>{{ selectedReport.futurePlans }}</p>
        </div>
        <div class="view-footer">
          <span><strong>작성자:</strong> {{ selectedReport.authorName }}</span>
          <span><strong>팀원:</strong> {{ selectedReport.teamInfo }}</span>
        </div>

        <!-- 피드백 컴포넌트 추가 -->
        <ReportFeedback
          :report-id="selectedReport.id"
          :is-professor="isProfessor"
          class="feedback-wrapper"
        />
      </div>

      <!-- 보고서 생성 -->
      <div v-else-if="isCreatingNew" class="report-create">
        <header class="report-header">
          <h1>새 주간 보고서 작성</h1>
        </header>
        <form @submit.prevent="submitReport" class="report-form-card">
          <div class="meta-section">
            <div class="meta-item">
              <span class="meta-label">프로젝트</span>
              <span class="meta-value">{{ projectName }}</span>
            </div>
            <div class="meta-item">
              <span class="meta-label">팀원</span>
              <span class="meta-value">{{ teamMemberNames }}</span>
            </div>
          </div>
          <div class="form-grid">
            <div class="form-group span-2">
              <label for="report-title">보고서 제목</label>
              <input id="report-title" type="text" v-model="newReport.title" placeholder="예: 1주차 주간 보고서" required />
            </div>
            <div class="form-group">
              <label for="report-period">보고 기간</label>
              <input id="report-period" type="text" v-model="newReport.reportingPeriod" placeholder="예: 2024.05.20 ~ 2024.05.26" required />
            </div>
            <div class="form-group">
              <label for="report-category">카테고리</label>
              <select id="report-category" v-model="newReport.category" required>
                <option disabled value="">카테고리 선택</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label for="progress-content">금주 진행 내용</label>
            <textarea id="progress-content" v-model="newReport.progressContent" rows="8" placeholder="이번 주에 진행한 작업 내용을 상세히 작성해주세요." required></textarea>
          </div>
          <div class="form-group">
            <label for="problems-solutions">문제점 및 해결 방안</label>
            <textarea id="problems-solutions" v-model="newReport.problemsAndSolutions" rows="4" placeholder="개발 과정에서 겪었던 어려움과 해결책을 작성해주세요."></textarea>
          </div>
          <div class="form-group">
            <label for="future-plans">향후 계획</label>
            <textarea id="future-plans" v-model="newReport.futurePlans" rows="4" placeholder="다음 보고서 기간까지의 목표와 계획을 작성해주세요." required></textarea>
          </div>
          <div class="form-actions">
            <button type="button" class="cancel-btn" @click="cancelCreation">취소</button>
            <button type="submit" class="submit-btn">보고서 제출</button>
          </div>
        </form>
      </div>

      <!-- 초기 화면 -->
      <div v-else class="empty-state">
        <div class="empty-inner">
          <span class="empty-icon">📄</span>
          <p>왼쪽에서 보고서를 선택하거나<br>'새 보고서 작성' 버튼을 눌러 시작하세요.</p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import axios from 'axios';
import ReportFeedback from '@/components/ReportFeedback.vue';

const reports = ref([]);
const selectedIndex = ref(null);
const isCreatingNew = ref(false);
const projectName = ref('');
const teamMembers = ref([]);
const userRole = ref('STUDENT');
const categories = ['기획', '설계', '개발', '테스트'];

const newReport = ref({
  title: '',
  reportingPeriod: '',
  authorName: '',
  category: '',
  progressContent: '',
  problemsAndSolutions: '',
  futurePlans: '',
  projectId: null,
});

const selectedReport = computed(() => (selectedIndex.value !== null ? reports.value[selectedIndex.value] : null));
const teamMemberNames = computed(() => teamMembers.value.map(m => m.userName).join(', '));
const isProfessor = computed(() => userRole.value === 'PROFESSOR');

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

const fetchReports = async (projectId) => {
  try {
    const { data } = await axios.get(`/reports/project/${projectId}`);
    reports.value = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    if (reports.value.length > 0 && selectedIndex.value === null) {
      selectedIndex.value = 0;
    }
  } catch (error) {
    console.error('보고서 목록 로딩 실패:', error);
  }
};

onMounted(async () => {
  try {
    const { data: me } = await axios.get('/auth/me');
    userRole.value = me.role;
    newReport.value.authorName = me.userName;
    newReport.value.projectId = me.projectId;

    const { data: project } = await axios.get(`/projects/${me.projectId}`);
    projectName.value = project.title;

    const { data: members } = await axios.get('/projects/members/students', {
      params: { projectId: me.projectId }
    });
    teamMembers.value = members;

    await fetchReports(me.projectId);

  } catch (error) {
    console.error('데이터 로딩 실패:', error);
    alert('필요한 정보를 불러오는 데 실패했습니다.');
  }
});

const selectReport = (index) => {
  selectedIndex.value = index;
  isCreatingNew.value = false;
};

const startNewReport = () => {
  selectedIndex.value = null;
  isCreatingNew.value = true;
  // Reset form
  Object.assign(newReport.value, {
    title: '',
    reportingPeriod: '',
    category: '',
    progressContent: '',
    problemsAndSolutions: '',
    futurePlans: '',
  });
};

const cancelCreation = () => {
  isCreatingNew.value = false;
  if (reports.value.length > 0) {
    selectedIndex.value = 0;
  }
};

const submitReport = async () => {
  if (!newReport.value.title || !newReport.value.category || !newReport.value.progressContent || !newReport.value.futurePlans) {
    alert('필수 항목을 모두 입력해주세요.');
    return;
  }

  const reportData = {
    ...newReport.value,
    teamInfo: teamMemberNames.value,
  };

  try {
    await axios.post('/reports', reportData);
    alert('보고서가 성공적으로 제출되었습니다.');
    isCreatingNew.value = false;
    await fetchReports(newReport.value.projectId);
  } catch (error) {
    console.error('보고서 제출 실패:', error);
    alert('보고서 제출 중 오류가 발생했습니다.');
  }
};

watch(isCreatingNew, (isCreating) => {
  if (!isCreating && reports.value.length > 0 && selectedIndex.value === null) {
    selectedIndex.value = 0;
  }
});
</script>

<style scoped>
.report-page-container {
  display: flex;
  height: calc(100vh - 61px); /* 헤더 높이 제외 */
  background: #f8f9fa;
}

/* --- 사이드바 --- */
.report-sidebar {
  width: 280px;
  background: #fff;
  border-right: 1px solid #e9ecef;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}
.sidebar-header {
  padding-bottom: 1rem;
  border-bottom: 1px solid #e9ecef;
}
.sidebar-header h3 {
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: #343a40;
}
.add-btn {
  width: 100%;
  padding: 10px;
  background: #3f8efc;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background-color 0.2s;
}
.add-btn:hover { background-color: #3578e5; }

.report-list {
  list-style: none;
  padding: 0;
  margin-top: 1rem;
  overflow-y: auto;
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
.report-list-title { display: block; font-size: 15px; }
.report-list-date { font-size: 12px; color: #868e96; }

/* --- 콘텐츠 영역 --- */
.report-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: #f8f9fa;
}

/* 보고서 조회 스타일 */
.report-view { background: #fff; padding: 2rem; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.view-header { border-bottom: 1px solid #e9ecef; padding-bottom: 1rem; margin-bottom: 1.5rem; }
.view-header h2 { font-size: 24px; font-weight: 700; margin-bottom: 0.5rem; }
.header-meta { display: flex; align-items: center; gap: 1rem; color: #495057; }
.category-badge { display: inline-block; padding: 4px 10px; font-size: 12px; font-weight: 700; border-radius: 12px; color: #fff; }
.category-기획 { background-color: #3498db; }
.category-설계 { background-color: #9b59b6; }
.category-개발 { background-color: #2ecc71; }
.category-테스트 { background-color: #f39c12; }
.view-section { margin-bottom: 1.5rem; }
.view-section h4 { font-size: 16px; font-weight: 600; color: #343a40; margin-bottom: 0.5rem; }
.view-section p { font-size: 15px; color: #495057; line-height: 1.7; white-space: pre-wrap; }
.view-footer { border-top: 1px solid #e9ecef; padding-top: 1rem; margin-top: 1.5rem; font-size: 14px; color: #868e96; display: flex; flex-direction: column; gap: 0.5rem; }
.feedback-wrapper {
  margin-top: 2rem;
}


/* 보고서 생성 스타일 (기존 스타일 재사용 및 수정) */
.report-create { max-width: 900px; margin: 0 auto; }
.report-header { text-align: center; margin-bottom: 2rem; }
.report-header h1 { font-size: 26px; font-weight: 800; color: #2c3e50; }
.report-form-card { background: #fff; border-radius: 12px; padding: 24px; box-shadow: 0 8px 24px rgba(0,0,0,0.08); }
.meta-section {
  background: #f7f9fc;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.meta-item {
  display: flex;
  align-items: center;
}
.meta-label {
  font-weight: 600;
  color: #34495e;
  width: 70px;
}
.meta-value {
  color: #2c3e50;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}
.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.form-group.span-2 {
  grid-column: span 2;
}
label {
  font-weight: 600;
  font-size: 15px;
  color: #34495e;
}
input, select, textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 15px;
  transition: border-color 0.2s, box-shadow 0.2s;
}
input:focus, select:focus, textarea:focus {
  outline: none;
  border-color: #3f8efc;
  box-shadow: 0 0 0 3px rgba(63, 142, 252, 0.2);
}
textarea {
  resize: vertical;
  min-height: 80px;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}
.cancel-btn {
  padding: 12px 24px;
  background-color: #f1f3f5;
  color: #495057;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}
.submit-btn {
  padding: 12px 24px;
  background-color: #3f8efc;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
}

/* 초기 화면 스타일 */
.empty-state { display: flex; justify-content: center; align-items: center; height: 100%; }
.empty-inner { text-align: center; color: #adb5bd; }
.empty-icon { font-size: 3rem; display: block; margin-bottom: 1rem; }
.empty-inner p { font-size: 1.1rem; line-height: 1.6; }
</style>