<template>
  <div
    ref="feedbackInputRef"
    class="feedback-input"
    :style="positionStyle"
  >
    <div class="category-selector">
      <label v-for="cat in categories" :key="cat.value" class="category-label" :class="{ active: selectedCategory === cat.value }">
        <input type="radio" v-model="selectedCategory" :value="cat.value" />
        <span class="icon">{{ cat.icon }}</span>
        <span>{{ cat.label }}</span>
      </label>
    </div>

    <textarea
      v-model="text"
      placeholder="피드백을 입력하세요"
    ></textarea>
    <div class="actions">
      <button @click="submit">등록</button>
      <button class="cancel" @click="$emit('close')">취소</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import axios from 'axios';

const props = defineProps({
  x: Number,
  y: Number,
  page: String,
  readonly: Boolean, // 🔥 읽기전용 여부 (true일 때만 등록 허용)
  projectId: Number
});

const emit = defineEmits(['submitted', 'close']);
const text = ref('');
const selectedCategory = ref('IMPROVEMENT'); // 기본값
const feedbackInputRef = ref(null);

const positionStyle = computed(() => {
  return {
    position: 'absolute',
    top: `${props.y - 100}px`,
    left: `${props.x - 300}px`,
    zIndex: 999,
  };
});

const handleClickOutside = (event) => {
  if (feedbackInputRef.value && !feedbackInputRef.value.contains(event.target)) {
    emit('close');
  }
};

onMounted(() => document.addEventListener('click', handleClickOutside, true));
onUnmounted(() => document.removeEventListener('click', handleClickOutside, true));


const categories = [
  { value: 'IMPROVEMENT', label: '개선 제안', icon: '💡' },
  { value: 'IDEA',        label: '아이디어', icon: '✨' },
  { value: 'COMPLIMENT',  label: '칭찬', icon: '👍' },
  { value: 'QUESTION',    label: '질문', icon: '❓' }
];

// 피드백 등록
const submit = async () => {
  if (!text.value.trim()) {
    alert('내용을 입력해주세요.')
    return
  }

  if (!props.readonly) {
    alert('읽기전용 모드일 때만 피드백을 작성할 수 있습니다.')
    return
  }

  try {
await axios.post('/feedbacks/create', {
  page: props.page,
  x: props.x,
  y: props.y,
  text: text.value,
  category: selectedCategory.value,
  projectId: props.projectId  // ✅ 여기!!
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
.feedback-input {
  width: 320px;
  background: white;
  border: 1px solid #3f8efc;
  border-radius: 8px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.category-selector {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 6px;
}

.category-label {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 8px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s;
}
.category-label:hover {
  background-color: #f5f5f5;
}
.category-label.active {
  background-color: #eef6ff;
  border-color: #3f8efc;
  color: #3f8efc;
  font-weight: 600;
}

textarea {
  width: 100%;
  height: 120px;
  resize: none;
  padding: 6px;
  border: 1px solid #bbb;
  border-radius: 4px;
  font-size: 14px;
  margin-bottom: 8px;
}
.category-label input[type="radio"] {
  display: none;
}
.icon { font-size: 16px; }

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 6px;
}

button {
  padding: 4px 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 13px;
}

button:hover {
  opacity: 0.9;
}

button.cancel {
  background-color: #eee;
  color: #444;
}

button:not(.cancel) {
  background-color: #3f8efc;
  color: white;
}
</style>
