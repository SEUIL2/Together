<template>
  <div
    class="feedback-note"
    :style="{ top: y + 'px', left: x + 'px', color: categoryColor }"
    @click="handleClick"
    v-html="markerSvg"
  >
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

const categoryColor = computed(() => {
  switch (props.category) {
    case 'IMPROVEMENT': return '#3498db'; // 개선 (파랑)
    case 'IDEA': return '#f1c40f'; // 아이디어 (노랑)
    case 'COMPLIMENT': return '#2ecc71'; // 칭찬 (초록)
    case 'QUESTION': return '#9b59b6'; // 질문 (보라)
    default: return '#e53935'; // 기본 (빨강)
  }
});

// SVG 아이콘을 사용하여 렌더링 일관성 확보
const markerSvg = computed(() => {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="${categoryColor.value}" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"></path>
      <circle cx="12" cy="10" r="3"></circle>
    </svg>
  `;
});

</script>

<style scoped>
.feedback-note {
  position: absolute;
  cursor: pointer;
  z-index: 50;
  transform: translate(-1200%, -500%); /* 💡 수정: 마커의 끝점이 좌표에 위치하도록 조정 */
  width: 24px;
  height: 24px;
}
/* v-html로 주입된 SVG 스타일링 */
.feedback-note > :deep(svg) {
  filter: drop-shadow(0 2px 3px rgba(0,0,0,0.3));
}
</style>
