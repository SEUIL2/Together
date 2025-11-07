<template>
  <div class="system-node vue-flow__node-resizable">
    <NodeResizer v-if="selected" :min-width="200" :min-height="150" color="#888" />

    <div v-if="!isEditing" class="system-label" @dblclick.stop.prevent="startEditing">{{ data.label || '시스템 경계' }}</div>
    <input v-else ref="inputRef" v-model="data.label" class="system-label-input" @blur="stopEditing" @keydown.enter="stopEditing" @keydown.stop />
  </div>
</template>

<script setup>
import { NodeResizer } from '@vue-flow/node-resizer'
import { defineProps, ref, nextTick } from 'vue'
// [수정] useNode, computed는 이 컴포넌트에서 필요 없습니다.

// [핵심] defineProps에 'selected'를 추가합니다.
// Vue Flow가 노드를 클릭할 때 이 prop을 true로 바꿔줍니다.
const props = defineProps({
  data: {
    type: Object,
    required: true,
  },
  selected: {
    type: Boolean,
    required: true,
  },
  id: { // id prop도 받는 것이 좋습니다.
    type: String,
    required: true,
  }
})

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
}
</script>

<style scoped>


.system-node {
  width: 100%;
  height: 100%;
  border: 2px dashed #888;
  border-radius: 8px;
  background-color: rgba(240, 240, 240, 0.2); /* 반투명 배경 */
  position: relative;
  padding: 20px;
}
.system-label,
.system-label-input {
  /* 라벨을 박스 왼쪽 상단에 배치 */
  position: absolute;
  top: -10px; /* 테두리 위에 걸치도록 */
  left: 15px;
  background-color: #f8f9fa; /* 캔버스 배경색과 동일하게 */
  padding: 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #555;
  border: none;
  outline: none;
}

.system-label-input {
  border: 1px solid #3b82f6;
  border-radius: 4px;
  top: -12px; /* input 테두리 두께만큼 살짝 위로 */
  padding: 2px 8px;
  width: auto;
}

.vue-flow__handle {
  opacity: 0.3;
  width: 10px;
  height: 10px;
  background: #aaa;
  border: 1px solid #fff;
}
</style>