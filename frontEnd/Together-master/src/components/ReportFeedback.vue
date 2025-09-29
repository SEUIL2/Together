<template>
  <div class="feedback-section">
    <h4 class="feedback-title">교수님 피드백</h4>
    
    <!-- 교수용 피드백 입력/수정 UI -->
    <div v-if="isProfessor" class="professor-feedback-area">
      <textarea
        v-model="feedbackContent"
        placeholder="피드백을 입력하세요..."
        class="feedback-textarea"
      ></textarea>
      <div class="feedback-actions">
        <button @click="deleteFeedback" class="action-btn delete-btn" :disabled="!feedbackContent">삭제</button>
        <button @click="saveFeedback" class="action-btn save-btn" :disabled="!feedbackContent">저장</button>
      </div>
    </div>

    <!-- 학생용 피드백 표시 UI -->
    <div v-else>
      <div v-if="feedbackContent" class="feedback-item">
        <p class="feedback-content">{{ feedbackContent }}</p>
      </div>
      <div v-else class="no-feedback">
        아직 등록된 피드백이 없습니다.
      </div>
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

const emit = defineEmits(['feedback-updated']);
const feedbackContent = ref('');

const fetchFeedbacks = async () => {
  if (!props.reportId) return;
  try {
    // 보고서 상세 정보를 가져와 feedback 필드를 사용합니다.
    const { data } = await axios.get(`/reports/${props.reportId}`);
    feedbackContent.value = data.feedback || '';
  } catch (error) {
    console.error('피드백 로딩 실패:', error);
    feedbackContent.value = ''; // 에러 발생 시 내용 초기화
  }
};

const saveFeedback = async () => {
  if (!props.reportId || !feedbackContent.value.trim()) return;
  try {
    await axios.post(`/reports/${props.reportId}/feedback`, {
      feedback: feedbackContent.value
    });
    alert('피드백이 저장되었습니다.');
    emit('feedback-updated');
  } catch (error) {
    console.error('피드백 저장 실패:', error);
    alert('피드백 저장에 실패했습니다.');
  }
};

const deleteFeedback = async () => {
  if (!props.reportId) return;
  if (!confirm('작성한 피드백을 삭제하시겠습니까?')) return;
  try {
    await axios.delete(`/reports/${props.reportId}/feedback`);
    feedbackContent.value = '';
    alert('피드백이 삭제되었습니다.');
    emit('feedback-updated');
  } catch (error) {
    console.error('피드백 삭제 실패:', error);
    alert('피드백 삭제에 실패했습니다.');
  }
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
.feedback-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 1rem;
  border: 1px solid #e9ecef;
}
.feedback-content { font-size: 14px; color: #495057; white-space: pre-wrap; }
.no-feedback { color: #868e96; padding: 1rem 0; }

.professor-feedback-area {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feedback-textarea {
  width: 100%;
  min-height: 120px;
  padding: 12px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 15px;
  resize: vertical;
}
.feedback-textarea:focus {
  outline: none;
  border-color: #3f8efc;
  box-shadow: 0 0 0 3px rgba(63, 142, 252, 0.2);
}

.feedback-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.action-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
}
.action-btn.save-btn {
  background-color: #3f8efc;
  color: white;
}
.action-btn.delete-btn {
  background-color: #f1f3f5;
  color: #495057;
}
.action-btn:disabled {
  background-color: #ced4da;
  cursor: not-allowed;
}
</style>