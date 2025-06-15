<template>
  <div
    class="context-menu"
    :style="{ left: x + 'px', top: y + 'px' }"
    @mousedown.stop
    @contextmenu.prevent
  >
    <!-- 노드용 메뉴 (액터/유스케이스) -->
    <template v-if="target && (target.type === 'actor' || target.type === 'usecase')">
      <button @click="onEdit">이름 변경</button>
      <button @click="onConnect">연결 생성</button>
      <button class="danger" @click="onDelete">삭제</button>
    </template>

    <!-- 관계선용 메뉴 -->
    <template v-else-if="target && target.type === 'link'">
      <button @click="onToggleType">
        종류 변경: {{ linkTypeLabel }}
      </button>
      <button class="danger" @click="onDelete">삭제</button>
    </template>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  x: { type: Number, required: true },
  y: { type: Number, required: true },
  target: { type: Object, required: true }
})
const emit = defineEmits([
  'close',
  'edit',      // (target)
  'delete',    // (target)
  'connect',   // (target)
  'toggle-type'// (target)
])

// 관계선 종류 label
const linkTypeLabel = computed(() => {
  if (props.target?.currentType === 'include') return 'Extend로 변경'
  if (props.target?.currentType === 'extend') return 'Include로 변경'
  return ''
})

// 메뉴 항목별 동작
const onEdit = () => {
  emit('edit', props.target)
  emit('close')
}
const onDelete = () => {
  emit('delete', props.target)
  emit('close')
}
const onConnect = () => {
  emit('connect', props.target)
  emit('close')
}
const onToggleType = () => {
  emit('toggle-type', props.target)
  emit('close')
}

// ESC, 바깥 클릭시 닫기
const onEsc = (e) => {
  if (e.key === 'Escape') emit('close')
}
window.addEventListener('keydown', onEsc)
</script>

<style scoped>
.context-menu {
  position: absolute;
  min-width: 140px;
  background: #fff;
  border: 1.2px solid #6667;
  box-shadow: 0 4px 16px #1976d23a, 0 0 1.5px #1976d233;
  border-radius: 9px;
  padding: 10px 0;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  animation: menuPop .16s cubic-bezier(.3,1.6,.6,1.0);
}
@keyframes menuPop {
  0% { opacity: 0; transform: scale(0.93);}
  100% { opacity: 1; transform: scale(1);}
}
.context-menu button {
  all: unset;
  cursor: pointer;
  font-size: 16px;
  color: #222;
  padding: 9px 18px;
  border-radius: 7px;
  margin: 0 4px;
  transition: background 0.14s;
  user-select: none;
}
.context-menu button:hover {
  background: #e3f2fd;
  color: #1976d2;
}
.context-menu .danger {
  color: #f44336;
  font-weight: 600;
}
.context-menu .danger:hover {
  background: #fdeaea;
}
</style>
