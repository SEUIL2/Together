<template>
  <div class="feedback-page-container">
    <header class="page-header">
      <h1>피드백 내역</h1>
      <p>교수님께서 남겨주신 피드백을 확인하고 관리합니다.</p>
    </header>

    <div v-if="loading" class="loading-state">
      <p>⏳ 피드백을 불러오는 중입니다...</p>
    </div>
    <div v-else-if="feedbacks.length === 0" class="empty-state">
      <p>🎉 교수님으로부터 받은 피드백이 아직 없습니다.</p>
    </div>

    <div v-else class="feedback-grid">
      <div v-for="fb in feedbacks" :key="fb.feedbackId" class="feedback-card" :class="{ 'is-read': fb.isRead }">
          <div class="card-header">
            <span class="feedback-page">{{ fb.page }}</span>
            <span class="feedback-date">{{ formatDate(fb.createdAt) }}</span>
          </div>
          <p class="feedback-text">{{ fb.text }}</p>
          <div class="card-footer">
            <span class="status-badge" :class="{ read: fb.isRead, unread: !fb.isRead }">{{ fb.isRead ? '읽음' : '안읽음' }}</span>
            <div class="footer-buttons">
              <button v-if="!fb.isRead" class="read-btn" @click="markAsRead(fb.feedbackId)">읽음</button>
              <button class="delete-btn" @click="deleteFeedback(fb.feedbackId)">삭제</button>
            </div>
          </div>
        </div>
      </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const loading = ref(true);

// 학생용 데이터
const feedbacks = ref([]);

onMounted(async () => {
  loading.value = true;
  try {
    await fetchFeedbacks();
  } catch (error) {
    console.error("페이지 로딩 중 오류 발생:", error);
  } finally {
    loading.value = false;
  }
});

const fetchFeedbacks = async () => {
  try {
    const { data } = await axios.get('/feedbacks/my');
    feedbacks.value = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
  } catch (err) {
    console.error('학생 피드백 불러오기 실패:', err);
    feedbacks.value = [];
  }
};

const markAsRead = async (feedbackId) => {
  try {
    await axios.post(`/feedbacks/${feedbackId}/read`, null);
    const feedback = feedbacks.value.find(fb => fb.feedbackId === feedbackId);
    if (feedback) feedback.isRead = true;
  } catch (err) {
    console.error('읽음 처리 실패:', err);
  }
};

const deleteFeedback = async (feedbackId) => {
  if (!confirm('이 피드백을 정말 삭제하시겠습니까?')) return;
  try {
    await axios.delete(`/feedbacks/${feedbackId}`);
    feedbacks.value = feedbacks.value.filter((fb) => fb.feedbackId !== feedbackId);
  } catch (err) {
    console.error('피드백 삭제 실패:', err);
    alert('삭제에 실패했습니다: ' + (err.response?.data?.message || err.message));
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.getMonth() + 1}월 ${d.getDate()}일 / ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
};
</script>

<style scoped>
.feedback-page-container { padding: 30px 40px; background-color: #f7f8fc; min-height: calc(100vh - 61px); }
.page-header { margin-bottom: 32px; text-align: center; }
.page-header h1 { font-size: 28px; font-weight: 800; color: #2c3e50; }
.page-header p { font-size: 16px; color: #7f8c8d; }

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
  position: relative;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.feedback-card:not(.is-read)::after {
  content: '';
  position: absolute;
  top: 10px;
  right: 10px;
  width: 10px;
  height: 10px;
  background-color: #3f8efc;
  border-radius: 50%;
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
.status-badge.unread { background-color: #eef6ff; color: #3f8efc; }
.footer-buttons {
  display: flex;
  align-items: center;
  gap: 8px;
}
.read-btn { background: #e9ecef; border: none; font-size: 12px; cursor: pointer; color: #495057; padding: 4px 10px; border-radius: 6px; font-weight: 500; }
.read-btn:hover { background: #dee2e6; }
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