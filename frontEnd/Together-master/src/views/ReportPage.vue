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
          <div class="view-header-main">
            <div class="view-header-left">
              <div class="title-wrapper">
                <h2>{{ selectedReport.title }}</h2>
                <span class="category-badge" :class="`category-${selectedReport.category}`">{{ categoryLabels[selectedReport.category] || '미지정' }}</span>
              </div>
              <span class="team-info"><strong>팀원:</strong> {{ teamMemberNames }}</span>
            </div>
            <div class="view-header-right">
              <button v-if="!isProfessor" class="pdf-btn" @click="exportReportAsPdf">PDF 추출</button>
              <div class="view-header-actions" v-if="!isProfessor && me.userName === selectedReport.authorName">
                <button class="edit-btn" @click="startEditingReport">수정</button>
                <button class="delete-btn" @click="deleteReport">삭제</button>
              </div>
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
          <h1>{{ newReport.id ? '주간 보고서 수정' : '새 주간 보고서 작성' }}</h1>
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
              <input id="report-period" type="text" v-model="newReport.period" placeholder="예: 2024.05.20 ~ 2024.05.26" required />
            </div>
            <div class="form-group">
              <label for="report-category">카테고리</label>
              <select id="report-category" v-model="newReport.category" required>
                <option disabled value="">카테고리 선택</option>
                <option v-for="cat in categories" :key="cat" :value="cat">{{ categoryLabels[cat] }}</option>
              </select>
            </div>
          </div>
          <div class="form-group">
            <label for="progress-content">금주 진행 내용</label>
            <textarea id="progress-content" v-model="newReport.weeklyProgress" rows="8" placeholder="이번 주에 진행한 작업 내용을 상세히 작성해주세요." required></textarea>
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
            <button type="submit" class="submit-btn">{{ newReport.id ? '수정 완료' : '보고서 제출' }}</button>
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
import { ref, onMounted, computed, watch } from 'vue'
import axios from '@/utils/axiosInstance.js' // Use the configured axios instance
import ReportFeedback from '@/components/ReportFeedback.vue';

const reports = ref([]);
const selectedIndex = ref(null);
const isCreatingNew = ref(false);
const projectName = ref('');
const teamMembers = ref([]);
const me = ref({ role: 'STUDENT', userName: '', projectId: null });
const categoryLabels = {
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트',
};
const categories = ['PLANNING', 'DESIGN', 'DEVELOPMENT', 'TEST'];

const newReport = ref({
  id: null,
  title: '',
  period: '',
  authorName: '',
  category: '',
  weeklyProgress: '',
  problemsAndSolutions: '',
  futurePlans: '',
  projectId: null,
});

const selectedReport = computed(() => (selectedIndex.value !== null ? reports.value[selectedIndex.value] : null));
const teamMemberNames = computed(() => teamMembers.value.map(m => m.userName).join(', '));
const isProfessor = computed(() => me.value.role === 'PROFESSOR');

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' });
};

const fetchReports = async () => {
  try {
    const { data } = await axios.get('/reports');
    if (Array.isArray(data)) {
      reports.value = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)); // createdAt이 null일 경우 대비
    } else {
      reports.value = [];
      console.error('보고서 목록 API가 배열을 반환하지 않았습니다:', data);
    }
    if (reports.value.length > 0 && selectedIndex.value === null) {
      selectedIndex.value = 0;
    }
  } catch (error) {
    console.error('보고서 목록 로딩 실패:', error);
  }
};

onMounted(async () => {
  try {
    const { data: meData } = await axios.get('/auth/me');
    me.value = meData;
    newReport.value.authorName = meData.userName;
    newReport.value.projectId = meData.projectId;
    
    // projectId가 유효할 때만 프로젝트 관련 정보를 가져옵니다.
    if (me.value.projectId) {
      const { data: project } = await axios.get(`/projects/${me.value.projectId}`);
      projectName.value = project.title;

      const { data: members } = await axios.get('/projects/members/students', {
        params: { projectId: me.value.projectId }
      });
      teamMembers.value = members;

      await fetchReports();
    } else {
      alert('프로젝트에 속해있지 않아 보고서 기능을 사용할 수 없습니다.');
    }
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
  // newReport의 id를 null로 설정하여 '생성' 모드임을 명확히 함
  Object.assign(newReport.value, {
    id: null,
    title: '',
    period: '',
    category: '',
    weeklyProgress: '',
    problemsAndSolutions: '',
    futurePlans: '',
  });
};

const startEditingReport = () => {
  if (!selectedReport.value) return;
  isCreatingNew.value = true;
  newReport.value = { ...selectedReport.value };
};

const cancelCreation = () => {
  isCreatingNew.value = false;
  if (reports.value.length > 0) {
    selectedIndex.value = 0;
  }
};

const submitReport = async () => {
  if (!newReport.value.title || !newReport.value.category || !newReport.value.weeklyProgress || !newReport.value.futurePlans) {
    alert('필수 항목을 모두 입력해주세요.');
    return;
  }

  try {
    if (newReport.value.id) {
      // 수정
      const { id, ...updateData } = newReport.value;
      delete updateData.authorName; // 불필요한 authorName 필드 제거
      const reportData = {
        ...updateData,
        teamInfo: teamMemberNames.value, // 수정 시에도 현재 팀원 정보 포함
      };
      await axios.put(`/reports/${id}`, reportData);
      alert('보고서가 성공적으로 수정되었습니다.');
    } else {
      // 생성
      const { authorName, ...restOfNewReport } = newReport.value; // authorName 필드 분리
      const reportData = {
        ...restOfNewReport,
        teamInfo: teamMemberNames.value,
      };
      await axios.post('/reports', reportData);
      alert('보고서가 성공적으로 제출되었습니다.');
    }
    isCreatingNew.value = false;
    await fetchReports();
    // 생성 또는 수정 후, 해당 보고서가 선택되도록 인덱스를 찾습니다.
    // 여기서는 목록의 첫 번째 항목을 선택하는 것으로 단순화합니다.
    selectedIndex.value = 0;
  } catch (error) {
    console.error('보고서 제출/수정 실패:', error);
    alert('보고서 처리 중 오류가 발생했습니다.');
  }
};

const exportReportAsPdf = async () => {
  if (!selectedReport.value) return;
  try {
    const response = await axios.get(
      `/reports/${selectedReport.value.id}/export`,
      {
        responseType: 'blob', // 파일 다운로드를 위해 필수
      }
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

const deleteReport = async () => {
  if (!selectedReport.value) return;
  if (!confirm('정말로 이 보고서를 삭제하시겠습니까?')) return;

  try {
    await axios.delete(`/reports/${selectedReport.value.id}`);
    alert('보고서가 삭제되었습니다.');
    selectedIndex.value = null; // 선택 해제
    await fetchReports();
  } catch (error) {
    console.error('보고서 삭제 실패:', error);
    alert('보고서 삭제 중 오류가 발생했습니다.');
  }
};

watch(isCreatingNew, (isCreating) => {
  if (!isCreating && reports.value.length > 0 && selectedIndex.value === null) {
    selectedIndex.value = 0;
  }
});

watch(selectedReport, (newVal) => {
  if (newVal && isCreatingNew.value) {
    isCreatingNew.value = false;
  }
});

/*
기존 코드
const submitReport = async () => {
  if (!newReport.value.title || !newReport.value.category || !newReport.value.weeklyProgress || !newReport.value.futurePlans) {
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
    await fetchReports();
  } catch (error) {
    console.error('보고서 제출 실패:', error);
    alert('보고서 제출 중 오류가 발생했습니다.');
  }
};

watch(isCreatingNew, (isCreating) => {
  if (!isCreating && reports.value.length > 0 && selectedIndex.value === null) {
    selectedIndex.value = 0;
  }
});*/
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
.view-header {
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 1rem;
  margin-bottom: 1.5rem;
}
.view-header-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start; /* 상단 정렬 */
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
.view-header-right { display: flex; flex-direction: column; align-items: flex-end; gap: 0.75rem; }
.view-header-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }
.view-header-actions button {
  background: none;
  border: 1px solid #ced4da;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 13px;
  cursor: pointer;
}
.view-header-actions .edit-btn:hover { background-color: #f1f3f5; }
.view-header-actions .delete-btn:hover { background-color: #fff5f5; color: #e03131; border-color: #ffc9c9; }

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