<template>
    <div class="toolbar">
      <h2>Toolbox</h2>
      <button @click="$emit('create-class')">클래스 생성</button>
      <button @click="$emit('toggle-relationship-mode')">
        {{ relationshipMode ? "연결선 모드 해제" : "연결선 추가 모드" }}
      </button>
    </div>
  </template>
  
  <script>
  export default {
    name: "Toolbar",
    props: {
      relationshipMode: {
        type: Boolean,
        default: false
      }
    }
  };
  </script>
  
<style scoped>
.toolbar {
  padding: 12px;
  width: 160px;               /* ← 너비 줄이기 */
  background: linear-gradient(135deg, #4a90e2, #50a7f4);
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  color: #fff;
  font-size: 14px;            /* ← 전체 글씨 작게 */
}

h2 {
  margin: 0 0 14px;
  font-size: 18px;            /* ← 제목 글씨 줄이기 */
  text-align: center;
  font-weight: bold;
}

button {
  width: 100%;
  padding: 8px 10px;          /* ← 버튼 내부 여백 줄이기 */
  font-size: 14px;
  margin-bottom: 10px;
  border: none;
  border-radius: 4px;
  background-color: #fff;
  color: #4a90e2;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s, transform 0.1s;
}

button:last-child {
  margin-bottom: 0;
}

button:hover {
  background-color: #e6f2ff;
  transform: translateY(-2px);
}
</style>
