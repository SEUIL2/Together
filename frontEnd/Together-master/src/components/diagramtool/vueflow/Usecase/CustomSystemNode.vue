<template>
  <!-- 
    이 노드는 다른 노드를 감싸는 '컨테이너' 역할을 할 수 있습니다. (parentNode 기능)
    우선은 단순한 점선 박스로 만듭니다.
  -->
  <div class="system-node vue-flow__node-resizable">
    <!-- [추가] 노드 크기 조절 핸들러 -->
    <NodeResizer v-if="selected" :min-width="200" :min-height="150" color="#888" />

    <!-- 라벨 (예: "결제 시스템") -->
    <!-- [수정] 더블클릭으로 이름 수정 가능하도록 변경 -->
    <div v-if="!isEditing" class="system-label" @dblclick.stop.prevent="startEditing">{{ data.label || '시스템 경계' }}</div>
    <input v-else ref="inputRef" v-model="data.label" class="system-label-input" @blur="stopEditing" @keydown.enter="stopEditing" @keydown.stop />
  </div>
</template>

<script setup>
import { NodeResizer } from '@vue-flow/node-resizer' // [추가] NodeResizer 임포트
import { defineProps, ref, nextTick, computed } from 'vue'
import { useNode } from '@vue-flow/core'

defineProps({
  data: {
    type: Object,
    required: true,
  },
})

// [추가] useNode 훅을 사용하여 노드의 현재 선택 상태를 가져옵니다.
const { node } = useNode()
const selected = computed(() => node.selected)

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
/* [추가] NodeResizer의 스타일을 임포트합니다. */
@import '@vue-flow/node-resizer/dist/style.css';

.system-node {
  /* [수정] 고정된 크기를 제거하고, 100%로 채우도록 변경 */
  width: 100%;
  height: 100%;
  
  /* 점선 테두리 */
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
