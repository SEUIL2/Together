<template>
  <div class="toolbar">
    <button @click="$emit('add-class')">+ 클래스 추가</button>
    <button @click="$emit('save-diagram')">저장하기</button>
  </div>
</template>

<script setup>
// Toolbar.vue는 emit만 전달
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

button {
  background-color: #444;
  border: none;
  padding: 6px 12px;
  border-radius: 4px;
  cursor: pointer;
  color: white;
  font-size: 14px;
}

button:hover {
  background-color: #666;
}
</style>
