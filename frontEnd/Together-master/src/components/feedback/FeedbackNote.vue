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
  return '#e53935'; // 기본 핀 색상
});

// SVG 아이콘을 사용하여 렌더링 일관성 확보
const markerSvg = computed(() => {
  return `
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="${categoryColor.value}" stroke="white" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
      <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"></path>
    </svg>
  `;
});

</script>

<style scoped>
.feedback-note {
  position: absolute;
  font-size: 22px;
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
