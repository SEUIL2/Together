<template>
  <div
    class="context-menu"
    :style="{ left: x + 'px', top: y + 'px' }"
    @mousedown.stop
    @contextmenu.prevent
  >
    <!-- 노드용 메뉴 -->
    <template v-if="target && (target.type === 'actor' || target.type === 'usecase')">
      <button @click="onEdit">이름 변경</button>
      <button @click="onConnect">연결 생성</button>
      <button class="danger" @click="onDelete">삭제</button>
    </template>

    <!-- 관계선용 메뉴 -->
    <template v-else-if="target && target.type === 'link'">
      <div class="dropdown-wrapper">
        <label>관계 종류</label>
        <select
          v-model="selectedType"
          @mousedown.stop
          @click.stop
        >
          <option value="association">기본 관계</option>
          <option value="include">include</option>
          <option value="extend">extend</option>
        </select>
      </div>
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
const emit = defineEmits(['close', 'edit', 'delete', 'connect', 'toggle-type'])

// 드롭다운 양방향 처리
const selectedType = computed({
  get() {
    return props.target?.currentType || 'association'
  },
  set(val) {
    emit('toggle-type', { ...props.target, nextType: val })
    emit('close')
  }
})

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

const onEsc = (e) => {
  if (e.key === 'Escape') emit('close')
}
window.addEventListener('keydown', onEsc)
</script>

<style scoped>
.context-menu {
  position: absolute;
  min-width: 160px;
  background: #fff;
  border: 1.2px solid #6667;
  box-shadow: 0 4px 16px #1976d23a, 0 0 1.5px #1976d233;
  border-radius: 9px;
  padding: 10px;
  z-index: 99999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  animation: menuPop 0.16s cubic-bezier(0.3, 1.6, 0.6, 1.0);
}
@keyframes menuPop {
  0% { opacity: 0; transform: scale(0.93); }
  100% { opacity: 1; transform: scale(1); }
}
.context-menu button {
  all: unset;
  cursor: pointer;
  font-size: 16px;
  color: #222;
  padding: 9px 12px;
  border-radius: 7px;
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
.dropdown-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.dropdown-wrapper select {
  font-size: 15px;
  padding: 6px;
  border-radius: 5px;
  border: 1px solid #ccc;
}
</style>
