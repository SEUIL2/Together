<template>
  <div class="actor-node">
    <Handle id="top" type="source" :position="Position.Top" :style="{ top: '10px' }" />
    <Handle id="top" type="target" :position="Position.Top" :style="{ top: '10px' }" />
    <!-- 
      기존 프로젝트의 actor.png 아이콘을 사용합니다. 
      (아이콘 크기에 맞춰 min-height 조절)
    -->
    <img :src="actorIcon" class="actor-icon" alt="Actor" />
    
    <!-- 
      노드의 라벨(이름)입니다.
      data.label로 전달되며, 더블클릭 시 수정 가능하도록 (나중에) 구현할 수 있습니다.
    -->
    <div class="label-container">
      <div v-if="!isEditing" class="actor-label" @dblclick.stop.prevent="startEditing">{{ data.label || '액터' }}</div>
      <input
        v-else
        ref="inputRef"
        v-model="data.label"
        class="actor-label-input"
        @blur="stopEditing"
        @keydown.enter="stopEditing"
        @keydown.stop
      />
    </div>

    <!-- 
      연결점(Handle)입니다. 
      이 점을 드래그해서 관계선을 만듭니다.
    -->
    <Handle id="bottom" type="source" :position="Position.Bottom" :style="{ bottom: '25px' }" />
    <Handle id="bottom" type="target" :position="Position.Bottom" :style="{ bottom: '25px' }" />
    <Handle id="left" type="source" :position="Position.Left" :style="{ left: '40px' }" />
    <Handle id="left" type="target" :position="Position.Left" :style="{ left: '40px' }" />
    <Handle id="right" type="source" :position="Position.Right" :style="{ right: '40px' }" />
    <Handle id="right" type="target" :position="Position.Right" :style="{ right: '40px' }" />
  </div>
</template>

<script setup>
import { Handle, Position } from '@vue-flow/core'
import { defineProps, ref, nextTick } from 'vue'
import actorIcon from '@/assets/actor.png' // [cite: uploaded:rjsdnmm1230/together/Together-7f2abf9c83de1a561c4de1239e75b98e9298501f/frontEnd/Together-master/src/assets/actor.png]

// data.label을 props로 받습니다.
const props = defineProps({
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
.actor-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-height: 80px; /* 아이콘 + 라벨 높이 */
  background-color: transparent; /* 배경 없음 */
  /* 테두리를 없애서 아이콘과 라벨만 보이게 합니다. */
  border: none; 
}
.actor-icon {
  width: 40px; /* 크기 조절 */
  height: 40px;
  object-fit: contain;
  margin: 8px 0; /* 위아래 여백을 주어 padding 효과를 대체합니다. */
}

/* 라벨과 인풋을 감싸는 컨테이너 */
.label-container {
  position: relative;
  width: 120px;
  min-height: 24px; /* 최소 높이 확보 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 텍스트 라벨과 입력창 공통 스타일 */
.actor-label,
.actor-label-input {
  font-size: 13px;
  color: #222;
  font-weight: 500;
  text-align: center;
  word-wrap: break-word;
  box-sizing: border-box;
  width: 100%;
  padding: 4px;
}

.actor-label-input {
  border: 1px solid #3b82f6;
  border-radius: 4px;
  outline: none;
}

/* 기본적으로 핸들(연결점)을 숨깁니다. */
.vue-flow__handle {
  opacity: 0;
  width: 12px;
  height: 12px;
  background: #3b82f6; /* Vue Flow 기본 파란색 */
  border: 2px solid #fff;
  box-shadow: 0 0 0 1.5px #3b82f6;
  transition: opacity 0.15s ease-in-out;
}

/* 노드 위에 마우스를 올렸을 때 핸들을 표시합니다. */
.vue-flow__node:hover .vue-flow__handle {
  opacity: 1;
}

.vue-flow__handle:hover {
  background: #1d4ed8;
}
</style>
