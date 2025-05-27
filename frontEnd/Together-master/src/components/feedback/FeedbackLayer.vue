<!-- 📍 src/components/feedback/FeedbackLayer.vue -->
<template>
  <div class="feedback-layer">
    <FeedbackNote
      v-for="note in feedbacks"
      :key="note.feedbackId"
      :x="note.x"
      :y="note.y"
      :text="note.text"
      :author="note.author"
      :feedback-id="note.feedbackId"
      :readonly="readonly"
      @click="handleNoteClick"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useFeedback } from '../../composables/useFeedback'
import FeedbackNote from '@/components/feedback/FeedbackNote.vue'


const props = defineProps<{
  page: string
  readonly: boolean
}>()

const feedbacks = ref<any[]>([])
const { getFeedbacksByPage, markFeedbackAsRead } = useFeedback()

const loadFeedbacks = async () => {
  feedbacks.value = await getFeedbacksByPage(props.page)
}

const handleNoteClick = async (feedbackId: number) => {
  await markFeedbackAsRead(feedbackId)
  feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== feedbackId)
}

onMounted(loadFeedbacks)

watch(() => props.page, loadFeedbacks)
</script>

<style scoped>
.feedback-layer {
  position: absolute;
  width: 100%;
  height: 100%;
  pointer-events: none; /* 기본적으로 투명하게 두고 */
}
.feedback-layer > * {
  pointer-events: auto; /* 피드백 박스만 클릭 가능하게 */
}
</style>
