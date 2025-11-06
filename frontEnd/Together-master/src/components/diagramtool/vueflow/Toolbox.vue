<template>
  <aside class="toolbox">
    <h3 class="toolbox-title">다이어그램 도구</h3>
    
    <!-- 1. 유스케이스 다이어그램 (기본으로 열림) -->
    <div class="toolbox-group">
      <button class="group-header" @click="toggleCategory('usecase')">
        <span>UML - 유스케이스</span>
        <span class="toggle-arrow" :class="{ 'is-open': openCategory === 'usecase' }">›</span>
      </button>
      
      <div class="group-items" v-if="openCategory === 'usecase'">
        <!-- 액터 (Actor) -->
        <div 
          class="tool-item" 
          draggable="true" 
          @dragstart="onDragStart($event, 'usecase-actor')"
        >
          <img :src="actorIcon" class="tool-icon" alt="Actor Icon" />
          <span class="tool-name">액터 (Actor)</span>
        </div>

        <!-- 유스케이스 (Use Case) -->
        <div 
          class="tool-item" 
          draggable="true" 
          @dragstart="onDragStart($event, 'usecase-bubble')"
        >
          <img :src="usecaseIcon" class="tool-icon oval" alt="Use Case Icon" />
          <span class="tool-name">유스케이스 (Use Case)</span>
        </div>

        <!-- 시스템 경계 (System Boundary) -->
        <div 
          class="tool-item" 
          draggable="true" 
          @dragstart="onDragStart($event, 'usecase-system')"
        >
          <!-- 시스템 경계 아이콘 (SVG) -->
          <svg class="tool-icon-svg" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="4" width="24" height="24" rx="2" stroke="#4B5563" stroke-width="2" stroke-dasharray="4 4"/>
          </svg>
          <span class="tool-name">시스템 경계</span>
        </div>

        <!-- 노트 (Note) -->
        <div 
          class="tool-item" 
          draggable="true" 
          @dragstart="onDragStart($event, 'note')"
        >
          <!-- 노트 아이콘 (SVG) -->
          <svg class="tool-icon-svg" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M6 6H22L26 10V26H6V6Z" stroke="#4B5563" stroke-width="2" fill="#FEFDF9"/>
            <path d="M22 6V10H26L22 6Z" stroke="#4B5563" stroke-width="2" fill="#E0E0E0"/>
          </svg>
          <span class="tool-name">노트 (Note)</span>
        </div>
      </div>
    </div>

    <!-- 2. 클래스 다이어그램 (닫혀 있음) -->
    <div class="toolbox-group">
      <button class="group-header" @click="toggleCategory('class')">
        <span>UML - 클래스</span>
        <span class="toggle-arrow" :class="{ 'is-open': openCategory === 'class' }">›</span>
      </button>
      
      <div class="group-items" v-if="openCategory === 'class'">
        <div 
          class="tool-item" 
          draggable="true" 
          @dragstart="onDragStart($event, 'classNode')"
        >
          <img :src="classIcon" class="tool-icon" alt="Class Icon" />
          <span class="tool-name">클래스 (Class)</span>
        </div>
      </div>
    </div>

    <!-- 3. ER 다이어그램 (닫혀 있음) -->
    <div class="toolbox-group">
      <button class="group-header" @click="toggleCategory('erd')">
        <span>데이터베이스 (ERD)</span>
        <span class="toggle-arrow" :class="{ 'is-open': openCategory === 'erd' }">›</span>
      </button>
      
      <div class="group-items" v-if="openCategory === 'erd'">
        <div 
          class="tool-item" 
          draggable="true" 
          @dragstart="onDragStart($event, 'entityNode')"
        >
          <img :src="entityIcon" class="tool-icon" alt="Entity Icon" />
          <span class="tool-name">엔티티 (Entity)</span>
        </div>
      </div>
    </div>
    
  </aside>
</template>

<script setup>
import { ref } from 'vue';

// 기존 프로젝트의 아이콘들을 임포트합니다.
import classIcon from '@/assets/classbox.png';
import actorIcon from '@/assets/actor.png';
import usecaseIcon from '@/assets/circle.png';
import entityIcon from '@/assets/table.png';

// 현재 열려있는 카테고리를 추적합니다. (기본값: 'usecase')
const openCategory = ref('usecase');

/**
 * 아코디언 메뉴를 토글합니다.
 * @param {string} categoryName 
 */
function toggleCategory(categoryName) {
  if (openCategory.value === categoryName) {
    openCategory.value = null; // 이미 열려있으면 닫기
  } else {
    openCategory.value = categoryName; // 다른 것이면 열기
  }
}

/**
 * 드래그 시작 시 이벤트 핸들러
 * @param {DragEvent} event
 * @param {string} nodeType - 캔버스에 추가할 노드의 타입
 */
function onDragStart(event, nodeType) {
  if (event.dataTransfer) {
    event.dataTransfer.setData('application/node', nodeType);
    event.dataTransfer.effectAllowed = 'copy';
  }
}
</script>

<style scoped>
/* 도구함 패널 스타일 (캔버스 위에 뜸)
  '오른쪽 중앙'에 고정됩니다.
*/
.toolbox {
  position: absolute;
  top: 50%; /* 세로 중앙 */
  right: 16px;
  transform: translateY(-50%); /* 정확한 중앙 정렬 */
  z-index: 10;
  
  width: 240px;
  max-height: calc(100% - 40px); /* 위아래 여백 */
  
  background-color: #fcfcfd;
  border-radius: 12px;
  border: 1px solid #e0e0e0;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
  
  display: flex;
  flex-direction: column;
}

.toolbox-title {
  font-size: 1rem;
  font-weight: 600;
  color: #333;
  padding: 16px;
  border-bottom: 1px solid #eee;
  margin: 0;
}

/* 스크롤 가능한 항목 영역 */
.toolbox-group {
  border-bottom: 1px solid #eee;
}
.toolbox-group:last-child {
  border-bottom: none;
}

/* 아코디언 헤더 (버튼) */
.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 14px 16px;
  
  background-color: #fff;
  border: none;
  cursor: pointer;
  
  font-size: 0.95rem;
  font-weight: 500;
  color: #222;
  text-align: left;
}
.group-header:hover {
  background-color: #f9f9f9;
}

.toggle-arrow {
  font-size: 1.2rem;
  font-weight: 600;
  color: #888;
  transition: transform 0.2s ease-in-out;
}
.toggle-arrow.is-open {
  transform: rotate(90deg);
}

/* 아코디언 내부 아이템 리스트 */
.group-items {
  padding: 8px 12px 16px 12px;
  background-color: #f7f7f7;
  border-top: 1px solid #eee;
  
  /* 부드러운 열림/닫힘 효과 (선택 사항) */
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

/* 드래그 가능한 도구 아이템 */
.tool-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #ddd;
  background-color: #fff;
  margin-bottom: 8px;
  cursor: grab;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(0,0,0,0.04);
}
.tool-item:last-child {
  margin-bottom: 0;
}

.tool-item:hover {
  border-color: #3b82f6;
  background-color: #f9faff;
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}

.tool-item:active {
  cursor: grabbing;
  background-color: #eef4ff;
}

.tool-icon {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  object-fit: contain;
}
.tool-icon.oval {
  padding: 4px; /* 유스케이스(타원) 아이콘용 여백 */
}

/* SVG 아이콘용 스타일 */
.tool-icon-svg {
  width: 32px;
  height: 32px;
  margin-right: 12px;
  flex-shrink: 0;
}

.tool-name {
  font-size: 0.95rem;
  color: #222;
  font-weight: 500;
}
</style>

