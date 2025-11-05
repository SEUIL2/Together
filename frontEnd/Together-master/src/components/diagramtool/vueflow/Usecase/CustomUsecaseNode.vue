<template>
  <div class="usecase-node">
    <div v-if="!isEditing" class="usecase-label" @dblclick.stop.prevent="startEditing">{{ data.label || '유스케이스' }}</div>
    <input
      v-else
      ref="inputRef"
      v-model="data.label"
      class="usecase-label-input"
      @blur="stopEditing"
      @keydown.enter="stopEditing"
      @keydown.stop
    />

    <!-- 
      [수정]
      Handle에 고유 id를 추가합니다.
    -->
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

const emit = defineEmits(['refocus-parent']);

const isEditing = ref(false)
const inputRef = ref(null)

const startEditing = () => {
  isEditing.value = true
  nextTick(() => {
    inputRef.value?.focus()
    inputRef.value?.select()
  })
}

const stopEditing = () => {
  isEditing.value = false
  emit('refocus-parent'); // 수정이 끝나면 부모에게 포커스를 되돌려달라고 알림
}
</script>

<style scoped>
.usecase-node {
  width: 140px;
  height: 70px;
  border-radius: 50%;
  background-color: #fff;
  border: 1.5px solid #333;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 10px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
}
.usecase-label,
.usecase-label-input {
  font-size: 13px;
  color: #222;
  font-weight: 500;
  width: 100%;
  word-wrap: break-word;
  text-align: center;
  background-color: transparent;
  border: none;
}

.usecase-label-input {
  padding: 4px;
  border: 1px solid #3b82f6;
  border-radius: 4px;
  outline: none;
  box-sizing: border-box;
}
.vue-flow__node:hover .vue-flow__handle {
  opacity: 1;
}
.vue-flow__handle {
  opacity: 0; /* 평소엔 숨김 */
  width: 12px;
  height: 12px;
  background: #3b82f6; /* Vue Flow 기본 파란색 */
  border: 2px solid #fff;
  box-shadow: 0 0 0 1.5px #3b82f6;
  transition: opacity 0.15s ease-in-out;
}

.vue-flow__handle:hover {
  background: #1d4ed8;
}
</style>
