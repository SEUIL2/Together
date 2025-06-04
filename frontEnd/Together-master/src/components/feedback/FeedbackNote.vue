<template>
  <div
    class="feedback-note"
    :style="{ top: y + 'px', left: x + 'px' }"
    @click="handleClick"
  >
    📌
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  x: number
  y: number
  feedbackId: number
  readonly: boolean
}>()

const emit = defineEmits<{
  (e: 'click', feedbackId: number): void
}>()

const handleClick = () => {
  if (props.readonly) emit('click', props.feedbackId)
}
</script>

<style scoped>
.feedback-note {
  position: absolute;
  font-size: 24px;
  cursor: pointer;
  z-index: 50;
}
</style>
