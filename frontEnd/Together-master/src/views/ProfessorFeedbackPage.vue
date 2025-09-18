<template>
  <div class="feedback-page-container">
    <header class="page-header">
      <h1>📝 작성한 피드백 내역</h1>
      <p>프로젝트를 선택하여 작성한 피드백을 확인하고 관리할 수 있습니다.</p>
    </header>

    <div class="controls-wrapper card">
      <div class="project-selector">
        <label for="project-select">프로젝트 선택:</label>
        <select id="project-select" v-model="selectedProjectId" @change="fetchFeedbacks">
          <option :value="null" disabled>-- 프로젝트를 선택하세요 --</option>
          <option v-for="project in projects" :key="project.projectId" :value="project.projectId">
            {{ project.title }}
          </option>
        </select>
      </div>
    </div>

    <div v-if="!selectedProjectId" class="empty-state"><p>먼저 프로젝트를 선택해주세요.</p></div>
    <div v-if="loading" class="loading-state">
      <p>⏳ 피드백을 불러오는 중입니다...</p>
    </div>
    <div v-else-if="feedbacks.length === 0" class="empty-state">
      <p>🎉 아직 작성한 피드백이 없습니다.</p>
    </div>

    <div v-else class="feedback-grid">
      <div v-for="fb in feedbacks" :key="fb.feedbackId" class="feedback-card" :class="{ 'is-read': fb.isRead }">
        <div class="card-header">
          <span class="feedback-page">{{ getPageDisplayName(fb.page) }}</span>
          <span class="feedback-date">{{ formatDate(fb.createdAt) }}</span>
        </div>
        <p class="feedback-text">{{ fb.text || fb.content }}</p>
        <div class="card-footer">
          <span class="status-badge" :class="{ read: fb.isRead, unread: !fb.isRead }">{{ fb.isRead ? '읽음' : '안읽음' }}</span>
          <button class="delete-btn" @click="deleteFeedback(fb.feedbackId)">삭제</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';

const loading = ref(true);
const feedbacks = ref([]);
const projects = ref([]);
const selectedProjectId = ref(null);

onMounted(async () => {
  try {
    await fetchProfessorProjects(); // 프로젝트 목록을 먼저 가져옵니다.
  } catch (error) {
    console.error("페이지 로딩 중 오류 발생:", error);
  } finally {
    loading.value = false;
  }
});
const fetchProfessorProjects = async () => {
  try {
    const { data: meData } = await axios.get('/auth/me');
    console.log('[1] 교수 정보 및 프로젝트 목록:', meData); // 디버깅 로그

    // meData 자체가 프로젝트 배열일 경우를 대비한 방어 코드 추가
    if (Array.isArray(meData)) {
      projects.value = meData;
      return;
    }
    projects.value = meData.projectId || [];
  } catch (error) {
    console.error('❌ [1-ERROR] 교수 프로젝트 목록 로딩 실패:', error.response || error);
    console.error('교수 프로젝트 목록 로딩 실패:', error);
    alert('관리 중인 프로젝트 목록을 불러오는 데 실패했습니다.');
  }
};

const fetchFeedbacks = async () => {
  if (!selectedProjectId.value) {
    feedbacks.value = [];
    return;
  }
  loading.value = true;
  try {
    console.log(`[2] 선택된 프로젝트 ID [${selectedProjectId.value}]에 대한 피드백을 요청합니다.`); // 디버깅 로그
    const { data } = await axios.get('/feedbacks/my', {
      params: { projectId: selectedProjectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    console.log('[3] API로부터 받은 원본 피드백 데이터:', data); // 디버깅 로그

    // API 응답이 배열인 경우에만 sort를 실행하여 오류를 방지합니다.
    if (Array.isArray(data)) {
      feedbacks.value = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    } else {
      console.warn('[3-WARN] API 응답이 배열이 아닙니다. 빈 배열로 처리합니다.'); // 디버깅 로그
      feedbacks.value = []; // 배열이 아니면 빈 배열로 초기화합니다.
    }
    console.log('[4] 화면에 표시될 최종 피드백 데이터:', feedbacks.value); // 디버깅 로그
  } catch (err) {
    console.error('❌ [2-ERROR] 교수 피드백 불러오기 실패:', err.response || err);
    feedbacks.value = [];
  } finally {
    loading.value = false;
  }
};

const deleteFeedback = async (feedbackId) => {
  if (!confirm('이 피드백을 정말 삭제하시겠습니까?')) return;
  try {
    await axios.delete(`/feedbacks/${feedbackId}`, {
      params: { projectId: selectedProjectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    feedbacks.value = feedbacks.value.filter((fb) => fb.feedbackId !== feedbackId);
  } catch (err) {
    console.error('피드백 삭제 실패:', err);
    alert('삭제에 실패했습니다: ' + (err.response?.data?.message || err.message));
  }
};

const getPageDisplayName = (pageSlug) => {
  const pageNames = {
    // 기획
    'planning-motivation': '기획-동기',
    'planning-goal': '기획-목표',
    'planning-requirement': '기획-요구사항',
    'planning-infostructure': '기획-정보구조도',
    'planning-storyboard': '기획-스토리보드',
    // 설계
    'design-sequence': '설계-시퀀스',
    'design-ui': '설계-UI',
    'design-table': '설계-테이블명세',
    'design-architecture': '설계-아키텍처',
    // 테스트
    'test-unit': '단위 테스트',
    'test-integration': '통합 테스트',
    // 기타
    'task-board': '작업 보드',
    'schedule-view': '일정관리',
    // 필요에 따라 다른 페이지 매핑 추가
  };
  return pageNames[pageSlug] || pageSlug;
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getMonth() + 1}월 ${d.getDate()}일 / ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.feedback-page-container { padding: 30px 40px; background-color: #f7f8fc; min-height: calc(100vh - 61px); }
.page-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #e9ecef;
  margin-bottom: 24px;
}
.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}
.page-header p { font-size: 16px; color: #6c757d; }

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.controls-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}
.project-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}
.project-selector label { font-weight: 600; }
.project-selector select {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ced4da;
  min-width: 250px;
}

.loading-state, .empty-state { text-align: center; padding: 60px 20px; color: #868e96; font-size: 16px; background-color: #fff; border-radius: 12px; }

.feedback-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}
.feedback-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  transition: transform 0.2s, box-shadow 0.2s;
  border: 1px solid #e9ecef;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}
.feedback-card.is-read {
  background-color: #f8f9fa;
}
.feedback-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.08);
}
.card-header { display: flex; justify-content: space-between; align-items: center; }
.feedback-page {
  font-weight: 600;
  color: #3f8efc;
  background-color: #eef6ff;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 13px;
}
.feedback-date { font-size: 12px; color: #868e96; }
.feedback-text { flex-grow: 1; font-size: 14px; color: #495057; line-height: 1.7; white-space: pre-wrap; max-height: 120px; overflow-y: auto; padding-right: 8px; }
.card-footer { display: flex; justify-content: space-between; align-items: center; margin-top: 12px; padding-top: 12px; border-top: 1px solid #f1f3f5; }
.status-badge {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
.status-badge.read { background-color: #f1f3f5; color: #868e96; }
.status-badge.unread { background-color: #ffe2e2; color: #d94848; }

.delete-btn {
  background: none;
  border: none;
  color: #adb5bd;
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  padding: 4px 6px;
}
.delete-btn:hover {
  color: #e53935;
}
</style>