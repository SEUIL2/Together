<template>
  <transition name="fade-scale">
    <div
      v-show="visible"
      class="feedback-popup"
      :style="{ top: (fb.y || 0) - 100 + 'px', left: (fb.x || 0) - 120 + 'px' }"
    >
      <div class="popup-content">
        <!-- 상단: 작성자 + 닫기 -->
        <div class="top-bar">
          <div class="author-info">
           <span v-if="categoryName" class="category-badge">
              📌 {{ categoryName }}
            </span>
            <span class="author">👤 {{ fb.author }}</span>
          </div>
          <button class="close-btn" @click="$emit('close')">✕</button>
        </div>

        <!-- 본문 -->
        <p class="text">“{{ fb.text }}”</p>

        <!-- 하단: 읽음 버튼 -->
        <div class="bottom-bar">
          <button class="mark-read-btn" @click="$emit('read', fb.feedbackId)">읽음</button>
        </div>

        <div class="arrow" />
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'

interface Feedback {
  feedbackId: number;
  author: string;
  categories: { id: number; name: string }[];
  text: string;
  x: number;
  y: number;
}

const props = defineProps<{ fb: Feedback }>()
defineEmits(['close', 'read'])

const categoryName = computed(() => props.fb?.categories?.[0]?.name || '');

const visible = ref(false)
onMounted(() => setTimeout(() => (visible.value = true), 0))
</script>

<style scoped>
/* 애니메이션 */
.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.25s ease;
}
.fade-scale-enter-from,
.fade-scale-leave-to {
  opacity: 0;
  transform: scale(0.95);
}

.feedback-popup {
  position: absolute;
  z-index: 999;
  transform: translateX(-50%);
  pointer-events: auto;
}

.popup-content {
  position: relative;
  background: #ffffff;
  border: 1px solid #0080ff;
  border-radius: 12px;
  padding: 14px 14px 14px;
  min-width: 300px;
  max-width: 380px;
  box-shadow: 0 6px 18px rgba(0, 128, 255, 0.2);
  font-family: 'SUIT', 'Noto Sans KR', sans-serif;
  color: #1f1f1f;
}

/* 상단 바 */
.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.author-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.category-badge {
  padding: 3px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
  background-color: #6c757d; /* 기본 회색 배경 */
}

.author {
  font-size: 14px;
  font-weight: 600;
  color: #007bff;
}

.close-btn {
  background: none;
  border: none;
  font-size: 16px;
  color: #777;
  cursor: pointer;
  transition: color 0.2s ease;
}
.close-btn:hover {
  color: #000;
}

/* 본문 텍스트 */
.text {
  font-size: 15.5px;
  line-height: 1.5;
  font-weight: 500;
  margin-bottom: 18px;
  white-space: pre-wrap;
}

/* 하단 바 */
.bottom-bar {
  display: flex;
  justify-content: flex-end;
}

.mark-read-btn {
  background-color: #007bff;
  color: white;
  border: none;
  font-size: 13px;
  padding: 5px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s ease;
}
.mark-read-btn:hover {
  background-color: #005ecb;
}

</style>
