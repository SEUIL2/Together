<template>
  <div class="feedback-section">
    <h4 class="feedback-title">교수님 피드백</h4>

    <!-- 피드백 목록 -->
    <div v-if="feedbacks.length > 0" class="feedback-list">
      <div v-for="fb in feedbacks" :key="fb.id" class="feedback-item">
        <div class="feedback-header">
          <span class="feedback-author">{{ fb.authorName }}</span>
          <span class="feedback-date">{{ formatDate(fb.createdAt) }}</span>
        </div>
        <p class="feedback-content">{{ fb.content }}</p>
      </div>
    </div>
    <div v-else class="no-feedback">
      아직 등록된 피드백이 없습니다.
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';
import axios from '@/utils/axiosInstance.js';

const props = defineProps({
  reportId: {
    type: Number,
    required: true,
  },
  isProfessor: {
    type: Boolean,
    default: false,
  },
});

const feedbacks = ref([]);

const fetchFeedbacks = async () => {
  if (!props.reportId) return;
  try {
    const { data } = await axios.get(`/report-feedbacks/report/${props.reportId}`);
    feedbacks.value = data;
  } catch (error) {
    console.error('피드백 로딩 실패:', error);
    feedbacks.value = []; // 에러 발생 시 목록 초기화
  }
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleString('ko-KR');
};

onMounted(fetchFeedbacks);

watch(() => props.reportId, fetchFeedbacks);
</script>

<style scoped>
.feedback-section {
  margin-top: 2.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e9ecef;
}
.feedback-title {
  font-size: 16px;
  font-weight: 600;
  color: #343a40;
  margin-bottom: 1rem;
}
.feedback-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.feedback-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid #e9ecef;
}
.feedback-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.5rem;
}
.feedback-author { font-weight: 600; color: #495057; }
.feedback-date { font-size: 12px; color: #868e96; }
.feedback-content { font-size: 14px; color: #495057; white-space: pre-wrap; }
.no-feedback { color: #868e96; padding: 1rem 0; }

</style>