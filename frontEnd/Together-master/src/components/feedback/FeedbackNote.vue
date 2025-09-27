<template>
  <div
    class="feedback-note"
    :style="{ top: y + 'px', left: x + 'px', color: categoryColor }"
    @click="handleClick"
  >
    {{ categoryIcon }}
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = defineProps<{
  x: number
  y: number
  feedbackId: number
  readonly: boolean
  category: 'IMPROVEMENT' | 'IDEA' | 'COMPLIMENT' | 'QUESTION' | string
}>()

const emit = defineEmits<{
  (e: 'click', feedbackId: number): void
}>()

const handleClick = () => {
  if (props.readonly) emit('click', props.feedbackId)
}

const categoryIcon = computed(() => {
  return '📌';
});

const categoryColor = computed(() => {
  return '#e53935'; // 기본 핀 색상
});
</script>

<style scoped>
.feedback-note {
  position: absolute;
  font-size: 22px;
  cursor: pointer;
  z-index: 50;
}
</style>
