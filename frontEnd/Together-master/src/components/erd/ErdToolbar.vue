<template>
    <div class="toolbar">
      <button @click="$emit('add-entity')">+ 테이블 추가</button>
      <button @click="$emit('export-json')">💾 저장</button>
      <button @click="$emit('import-json')">📂 불러오기</button>
      <button @click="$emit('clear-all')">🧹 전체삭제</button>
    </div>
  </template>
  
  <script setup>
  // 상태는 상위 컴포넌트(ErdDiagramPage.vue or ErdCanvas.vue)에서 관리
  </script>
  
  <style scoped>
  .toolbar {
    display: flex;
    gap: 12px;
    padding: 10px 20px;
    background-color: #1f2937;
    color: white;
    align-items: center;
    border-bottom: 2px solid #374151;
  }
  
  button {
    background: #374151;
    color: white;
    padding: 8px 14px;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-weight: 500;
  }
  
  button:hover {
    background: #4b5563;
  }
  </style>
  