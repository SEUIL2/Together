<template>
  <div class="note-node">
    <div class="note-corner"></div>
    <!-- [수정] 더블클릭으로 텍스트 편집 가능하도록 변경 -->
    <div v-if="!isEditing" class="note-label" @dblclick.stop.prevent="startEditing">{{ data.label || '노트...' }}</div>
    <textarea
      v-else
      ref="textareaRef"
      v-model="data.label"
      class="note-textarea"
      @blur="stopEditing"
      @keydown.stop
    ></textarea>

    <Handle id="top" type="source" :position="Position.Top" />
    <Handle id="top" type="target" :position="Position.Top" />
    <Handle id="bottom" type="source" :position="Position.Bottom" />
    <Handle id="bottom" type="target" :position="Position.Bottom" />
    <Handle id="left" type="source" :position="Position.Left" />
    <Handle id="left" type="target" :position="Position.Left" />
    <Handle id="right" type="source" :position="Position.Right" />
    <Handle id="right" type="target" :position="Position.Right" />
  </div>
</template>

<script setup>
import { Handle, Position } from '@vue-flow/core'
import { defineProps, ref, nextTick } from 'vue'

defineProps({
  data: {
    type: Object,
    required: true,
  },
})

const isEditing = ref(false)
const textareaRef = ref(null)

const startEditing = () => {
  isEditing.value = true
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

const stopEditing = () => {
  isEditing.value = false
}
</script>

<style scoped>
.note-node {
  width: 150px;
  min-height: 80px;
  background: #fffbe6; /* 연한 노란색 */
  border: 1px solid #c8c0a0;
  border-radius: 2px;
  padding: 10px;
  font-size: 13px;
  color: #5a523c;
  box-shadow: 2px 2px 5px rgba(0,0,0,0.05);
  position: relative;
}
/* 접힌 귀퉁이 모양 */
.note-corner {
  position: absolute;
  top: 0;
  right: 0;
  width: 0;
  height: 0;
  border-style: solid;
  border-width: 0 16px 16px 0; /* 16px 크기 */
  border-color: transparent #e0d9b4 transparent transparent;
  background-color: #f7f3d9; /* 안쪽 색 */
}
.note-label {
  width: 100%;
  height: 100%;
  word-wrap: break-word;
  white-space: pre-wrap; /* 줄바꿈과 공백 유지 */
}
.note-textarea {
  width: 100%;
  height: 100%;
  border: 1px solid #8e866b;
  border-radius: 2px;
  background: transparent;
  resize: none; /* 사용자가 크기 조절 못하게 */
  outline: none;
  font-family: inherit;
  font-size: inherit;
  color: inherit;
}
/* 핸들 스타일 */
.vue-flow__node:hover .vue-flow__handle {
  opacity: 1;
}
.vue-flow__handle {
  opacity: 0;
  background: #8e866b;
  border: 1px solid #fff;
}
</style>
