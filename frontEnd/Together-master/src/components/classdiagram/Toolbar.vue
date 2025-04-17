<template>
  <div class="toolbar">
    <button @click="$emit('add-class')">+ 클래스 추가</button>
    <button @click="$emit('export-json')">저장</button>
    <button @click="$emit('import-json')">불러오기</button>
    <button @click="$emit('clear-all')">전체삭제</button>
  </div>
</template>

<script setup>
// Toolbar.vue - 상태는 상위(CanvasArea.vue)에서 관리
// 여기서는 필요한 이벤트만 emit 처리
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  padding: 10px;
  background-color: #222;
  color: white;
  align-items: center;
}

.arrow-icon {
  cursor: grab;
  font-size: 20px;
  padding: 4px 8px;
  background: #444;
  border-radius: 4px;
  user-select: none;
}

.arrow-icon:hover {
  background: #666;
}
</style>
