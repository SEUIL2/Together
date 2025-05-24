<template>
  <div class="faq-card">
    <!-- 질문 -->
    <div class="faq-question" @click="toggle">
      <span class="question-text">{{ faq.question }}</span>
      <span class="arrow" :class="{ open }">▼</span>
    </div>

    <!-- 답변 -->
    <transition name="faq">
      <div v-if="open" class="faq-answer">
        {{ faq.answer }}
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  faq: {
    type: Object,
    required: true
  }
})

const open = ref(false)
const toggle = () => {
  open.value = !open.value
}
</script>

<style scoped>
.faq-card {
  border: 1px solid #ddd;
  border-radius: 12px;
  margin-bottom: 14px;
  background-color: #ffffff;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
  transition: box-shadow 0.3s ease;
}

.faq-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.faq-question {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  font-weight: 600;
  font-size: 16px;
  cursor: pointer;
  color: #333;
}

.arrow {
  transition: transform 0.3s ease;
  font-size: 18px;
  color: #888;
}

.arrow.open {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 14px 20px;
  font-size: 15px;
  color: #555;
  border-top: 1px solid #eee;
  background-color: #f9f9f9;
}

/* 트랜지션 효과 */
.faq-enter-active, .faq-leave-active {
  transition: all 0.3s ease;
}
.faq-enter-from, .faq-leave-to {
  opacity: 0;
  max-height: 0;
}
.faq-enter-to, .faq-leave-from {
  opacity: 1;
  max-height: 500px;
}
</style>
