<!-- 📍 src/components/feedback/FeedbackNote.vue -->
<template>
  <div
    class="feedback-note"
    :style="{
      top: y + 'px',
      left: x + 'px',
      backgroundColor: readonly ? '#f0f8ff' : '#ffe9e9',
    }"
    @click="handleClick"
  >
    <p class="feedback-text">{{ text }}</p>
    <small class="author-name">👤 {{ author }}</small>
  </div>
</template>

<script setup lang="ts">
const props = defineProps<{
  x: number
  y: number
  text: string
  author: string
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
  max-width: 200px;
  padding: 8px 10px;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.2);
  cursor: pointer;
  z-index: 50;
}
.feedback-text {
  font-size: 14px;
  margin: 0;
}
.author-name {
  display: block;
  font-size: 11px;
  color: #666;
  margin-top: 4px;
  text-align: right;
}
</style>
