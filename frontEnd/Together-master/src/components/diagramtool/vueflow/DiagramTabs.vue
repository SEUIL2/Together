<template>
  <div class="tabs-container">
    <nav class="tabs-nav">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        :class="['tab-button', { active: activeTabId === tab.id }]"
        @click="selectTab(tab.id)"
      >
        {{ tab.name }}
      </button>
    </nav>
  </div>
</template>

<script setup>
import { ref } from 'vue';

// 부모 컴포넌트에서 현재 활성화된 탭 ID를 받아옴
defineProps({
  activeTabId: {
    type: String,
    required: true,
  },
});

// 부모 컴포넌트로 탭 변경 이벤트를 보냄
const emit = defineEmits(['tab-changed']);

// 백엔드 구조에 맞는 고정된 탭 목록
// (id는 DesignDetailService의 'type'과 일치시키는 것이 좋음)
const tabs = ref([
  { id: 'infostructure', name: '정보구조도' }, // 백엔드 type 'infostructure'와 일치
  { id: 'usecase', name: '유스케이스' },
  { id: 'erd', name: 'ERD' },
  { id: 'classDiagram', name: '클래스 다이어그램' },
  { id: 'sequence', name: '시퀀스 다이어그램' },
  
]);

// 탭 클릭 시 부모에게 'tab-changed' 이벤트를 발생시킴
function selectTab(tabId) {
  emit('tab-changed', tabId);
}
</script>

<style scoped>
.tabs-container {
  background-color: #ffffff;
  padding: 0 16px;
  border-bottom: 2px solid #e0e0e0;
  flex-shrink: 0; /* 높이가 줄어들지 않음 */
  overflow-x: auto; /* 탭이 많아지면 스크롤 */
}

.tabs-nav {
  display: flex;
  align-items: center;
  height: 50px; /* 탭 높이 고정 */
}

.tab-button {
  padding: 0 20px;
  height: 100%;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 15px;
  font-weight: 500;
  color: #555;
  border-bottom: 3px solid transparent;
  transition: all 0.2s ease;
  white-space: nowrap; /* 탭 이름이 줄바꿈되지 않도록 */
}

.tab-button:hover {
  background-color: #f5f5f5;
  color: #000;
}

.tab-button.active {
  color: #3b82f6;
  font-weight: 600;
  border-bottom-color: #3b82f6;
}
</style>

