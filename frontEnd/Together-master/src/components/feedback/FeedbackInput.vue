<template>
  <div class="feedback-modal-overlay" @click.self="$emit('close')">
    <div class="feedback-container">
      <div
        ref="feedbackInputRef"
        class="feedback-input"
      >
        <div class="category-selector">
          <div class="category-list">
            <label v-for="cat in categories" :key="cat.id" class="category-label" :class="{ active: selectedCategoryId === cat.id }">
              <input type="radio" v-model="selectedCategoryId" :value="cat.id" />
              <span class="icon">{{ cat.icon }}</span>
              <span>{{ cat.label }}</span>
            </label>
          </div>
        </div>
  
        <textarea
          v-model="text"
          placeholder="피드백을 입력하세요"
        ></textarea>
        <div class="actions">
          <button @click="submit">등록</button>
          <button class="cancel" @click="$emit('close')">취소</button>
          <button class="history-btn" @click.stop="toggleHistory">내역</button>
        </div>
      </div>
  
      <!-- 피드백 내역 팝업 -->
      <div v-if="showHistory" class="history-popup" ref="historyPopupRef">
        <div v-if="feedbackHistory.length === 0" class="history-empty">작성 내역이 없습니다.</div>
        <ul v-else>
          <li v-for="item in feedbackHistory" :key="item.id" @click="applyHistory(item.text)">
            {{ item.text }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import api from '@/api';

const props = defineProps({
  x: Number,
  y: Number,
  page: String,
  readonly: Boolean, // 🔥 읽기전용 여부 (true일 때만 등록 허용)
  projectId: Number
});

const emit = defineEmits(['submitted', 'close']);
const text = ref('');
const selectedCategoryId = ref(null);
const feedbackInputRef = ref(null);
const showHistory = ref(false);
const feedbackHistory = ref([]);

const categories = ref([]);

const fetchFeedbackHistory = async () => {
  try {
    const response = await api.get('/feedbacks/history', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    feedbackHistory.value = response.data;
  } catch (err) {
    console.error('❌ 피드백 내역 불러오기 실패:', err);
    alert('피드백 내역을 불러오는 데 실패했습니다.');
  }
};

const toggleHistory = () => {
  showHistory.value = !showHistory.value;
  if (showHistory.value) {
    fetchFeedbackHistory();
  }
};

const applyHistory = (historyText) => {
  text.value = historyText;
  showHistory.value = false;
};

const fetchCategories = async () => {
  try {
    const response = await api.get('/feedbacks/categories', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    categories.value = response.data.map(cat => ({
      id: cat.id,      // DTO의 id를 value로 사용
      label: cat.name, // DTO의 name을 label로 사용
      icon: '📌' // 모든 카테고리에 기본 아이콘 적용
    }));
    if (categories.value.length > 0 && !categories.value.some(c => c.id === selectedCategoryId.value)) {
      selectedCategoryId.value = categories.value[0]?.id || null;
    }
  } catch (err) {
    console.error('❌ 카테고리 불러오기 실패:', err);
    alert('카테고리 목록을 불러오는 데 실패했습니다.');
  }
};

onMounted(() => {
  fetchCategories();
});

// 피드백 등록
const submit = async () => {
  if (!text.value.trim()) {
    alert('내용을 입력해주세요.')
    return
  }

  if (!selectedCategoryId.value) {
    alert('카테고리를 선택해주세요.');
    return;
  }

  console.log('피드백 전송 카테고리 ID:', selectedCategoryId.value); // 전송될 카테고리 값 확인
  if (!props.readonly) {
    alert('읽기전용 모드일 때만 피드백을 작성할 수 있습니다.')
    return
  }

  try {
await api.post('/feedbacks/create', {
  page: props.page,
  x: props.x,
  y: props.y,
  text: text.value,
  categoryIds: [selectedCategoryId.value], // 카테고리 ID를 배열 형태로 전송
  projectId: props.projectId
}, {
  headers: {
    Authorization: localStorage.getItem('authHeader')
  },
  withCredentials: true
})


    emit('submitted')
    emit('close')
  } catch (err) {
    console.error('❌ 피드백 저장 실패:', err)
    alert('피드백 저장에 실패했습니다.')
  }
}
</script>

<style scoped>
.feedback-modal-overlay {
  position: fixed;
  inset: 0;
  background-color: rgba(249, 250, 251, 0.5); /* 반투명한 밝은 배경으로 변경 */
  backdrop-filter: blur(4px); /* 배경을 흐리게 만드는 효과 */
  -webkit-backdrop-filter: blur(4px); /* Safari 호환성 */
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 998;
}

.feedback-container {
  position: relative; /* 자식 요소의 absolute 위치 기준점 */
}

.feedback-input {
  position: relative; /* z-index 적용을 위해 */
  width: 550px;
  background: white;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1); /* 그림자를 더 강조 */
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  z-index: 1;
  font-family: 'SUIT', 'Noto Sans KR', sans-serif;
}

.category-selector {
  overflow-x: auto;
  padding-bottom: 8px; /* 스크롤바 공간 확보 */
  margin-bottom: -8px; /* 확보한 공간만큼 다시 줄임 */
  -ms-overflow-style: none; /* IE and Edge */
  scrollbar-width: none; /* Firefox */
}
.category-selector::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */
}

.category-list {
  display: flex;
  gap: 8px;
  padding-bottom: 2px; /* 그림자 잘림 방지 */
}

.category-label {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
  white-space: nowrap; /* 카테고리 이름이 길어도 줄바꿈 방지 */
  flex-shrink: 0; /* 아이템이 줄어들지 않도록 설정 */
}
.category-label:hover {
  background-color: #f5f5f5;
  border-color: #ccc;
}
.category-label.active {
  background-color: #eef6ff;
  border-color: #3f8efc;
  color: #3f8efc;
  font-weight: 700;
}

textarea {
  width: 100%;
  height: 200px;
  resize: none;
  padding: 12px;
  border: 1px solid #ced4da;
  border-radius: 8px;
  font-size: 15px;
  line-height: 1.6;
  transition: border-color 0.2s, box-shadow 0.2s;
}
textarea:focus {
  outline: none;
  border-color: #3f8efc;
  box-shadow: 0 0 0 3px rgba(63, 142, 252, 0.2);
}
.category-label input[type="radio"] {
  display: none;
}
.icon { font-size: 18px; }

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

button {
  padding: 8px 16px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

button.history-btn {
  background-color: #6c757d;
  color: white;
  margin-left: auto; /* 버튼을 왼쪽으로 밀어냄 */
}

button:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

button.cancel {
  background-color: #f1f3f5;
  color: #495057;
}

button:not(.cancel) {
  background-color: #3f8efc;
  color: white;
}

.history-popup {
  position: absolute;
  top: 0; /* 입력창과 같은 높이에서 시작 */
  left: calc(100% + 16px); /* 입력창 바로 오른쪽에 16px 간격을 두고 위치 */
  width: 300px;
  height: 100%;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #dee2e6;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  padding: 8px;
}

.history-popup ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.history-popup li {
  padding: 10px 12px;
  font-size: 14px;
  color: #333;
  cursor: pointer;
  border-radius: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-popup li:hover {
  background-color: #f5f5f5;
}

.history-empty {
  padding: 20px;
  text-align: center;
  color: #888;
  font-size: 14px;
}
</style>
