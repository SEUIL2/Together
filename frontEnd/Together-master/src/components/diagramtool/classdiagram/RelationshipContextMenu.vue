<template>
  <div
    v-if="rel"
    class="custom-context-menu"
    :style="{ top: y + 'px', left: x + 'px' }"
    @click.stop
  >
    <div class="menu-section">
      <div class="menu-label">시작점</div>
      <select v-model="localRel.fromType" class="menu-select">
        <option value="none">없음</option>
        <option value="arrow">화살표 (의존)</option>
        <option value="triangle">삼각형 (일반화)</option>
        <option value="empty_diamond">빈 다이아몬드 (집합)</option>
        <option value="filled_diamond">채운 다이아몬드 (복합)</option>
      </select>
    </div>

    <div class="menu-section">
      <div class="menu-label">선 스타일</div>
      <select v-model="localRel.lineStyle" class="menu-select">
        <option value="solid">실선</option>
        <option value="dashed">점선</option>
      </select>
    </div>

    <div class="menu-section">
      <div class="menu-label">끝점</div>
      <select v-model="localRel.toType" class="menu-select">
        <option value="none">없음</option>
        <option value="arrow">화살표 (의존)</option>
        <option value="triangle">삼각형 (일반화)</option>
        <option value="empty_diamond">빈 다이아몬드 (집합)</option>
        <option value="filled_diamond">채운 다이아몬드 (복합)</option>
      </select>
    </div>

    <div class="menu-actions">
      <div class="menu-item danger" @click="$emit('delete')">
        <span class="icon">🗑️</span> 관계선 삭제
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'

const props = defineProps({
  rel: Object,
  x: Number,
  y: Number
})
const emit = defineEmits(['update', 'delete'])

const localRel = reactive({
  fromType: props.rel.fromType,
  toType:   props.rel.toType,
  lineStyle:  props.rel.lineStyle,
  bendStyle:  props.rel.bendStyle  ?? 'one'
})

watch(localRel, (newVal) => {
  emit('update', {
    ...props.rel,
    fromType:   newVal.fromType,
    toType:     newVal.toType,
    lineStyle:  newVal.lineStyle,
    bendStyle:  newVal.bendStyle
  })
}, { deep: true })

watch(() => props.rel, (newRel) => {
  if (newRel) {
    localRel.fromType = newRel.fromType;
    localRel.toType = newRel.toType;
    localRel.lineStyle = newRel.lineStyle;
    localRel.bendStyle = newRel.bendStyle ?? 'one';
  }
}, { deep: true });
</script>


<style scoped>
.custom-context-menu {
  position: absolute;
  z-index: 1000;
  min-width: 220px;
  background: #fff;
  border-radius: 11px;
  box-shadow: 0 4px 24px 0 #2230462a;
  border: 1.5px solid #e8eaf0;
  padding: 12px 10px;
  animation: pop-in 0.12s cubic-bezier(.41,.84,.67,1.2);
  user-select: none;
  font-family: 'Pretendard', sans-serif;
}
@keyframes pop-in {
  0% { transform: scale(0.95); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}

.menu-section {
  padding: 4px;
  margin-bottom: 8px;
}

.menu-label {
  font-weight: 600;
  font-size: 13px;
  color: #475569;
  margin-bottom: 6px;
  padding-left: 4px;
}

.menu-select {
  width: 100%;
  padding: 8px 10px;
  border-radius: 8px;
  border: 1px solid #cbd5e1;
  background-color: #fff;
  font-size: 14px;
  -webkit-appearance: none;
  appearance: none;
  background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='M6 8l4 4 4-4'/%3e%3c/svg%3e");
  background-position: right 0.5rem center;
  background-repeat: no-repeat;
  background-size: 1.5em 1.5em;
}

.menu-actions {
  margin-top: 8px;
  border-top: 1px solid #e2e8f0;
  padding-top: 8px;
}

.menu-item {
  display: flex;
  align-items: center;
  font-size: 15px;
  border-radius: 7px;
  padding: 8px 6px 8px 7px;
  margin-bottom: 2px;
  color: #2c3e50;
  cursor: pointer;
  transition: background 0.15s, color 0.18s;
}
.menu-item .icon {
  font-size: 17px;
  margin-right: 8px;
}
.menu-item.danger {
  color: #e44e5c;
  font-weight: 500;
}
.menu-item.danger:hover {
  background: #ffe6e7;
  color: #d7263d;
}

.delete-btn {
  z-index: 999;
}
</style>
