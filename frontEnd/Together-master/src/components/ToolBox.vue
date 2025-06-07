<template>
  <div class="toolbox">
    <!-- 도형 아이콘 목록 (상단 고정) -->
    <div class="icon-grid">
      <div
        v-for="tool in toolButtons[currentDiagram]"
        :key="tool.label"
        class="icon-button"
        :class="{ selected: isSelected(tool) }"
        draggable="true"
        @click="selectTool(tool)"
        @dragstart="onDragStart(tool, $event)"
      >
        <img :src="tool.icon" :alt="tool.label" class="icon-image" />
        <span class="icon-label">{{ tool.label }}</span>
      </div>
    </div>

    <!-- 다이어그램 종류 선택 탭 (아래 고정) -->
    <div class="diagram-tabs">
      <button
        v-for="type in diagramTypes"
        :key="type.value"
        :class="['tab-btn', { active: currentDiagram === type.value }]"
        @click="currentDiagram = type.value"
      >
        {{ type.label }}
      </button>
    </div>
  </div>
</template>


<script setup>
import { ref } from 'vue'
import { useToolStore } from '@/stores/toolStore'

const toolStore = useToolStore()
const currentDiagram = ref('class')

const diagramTypes = [
  { label: '클래스', value: 'class' },
  { label: 'ERD', value: 'erd' },
  { label: '정보구조도', value: 'info' },
  { label: '유스케이스', value: 'usecase' }
]

const toolButtons = {
class: [
  {
    label: '클래스 박스',
    type: 'box',
    subtype: 'class',
    icon: new URL('@/assets/classbox.png', import.meta.url).href  // ✅ 이렇게 처리
  }

  ],
  erd: [
    { label: '테이블', type: 'box', subtype: 'table', icon: '/assets/tool-icons/table.svg' },
  ],
  info: [
    { label: '페이지', type: 'box', subtype: 'page', icon: '/assets/tool-icons/page.svg' },
    { label: '링크', type: 'relationship', subtype: 'link', icon: '/assets/tool-icons/link.svg' }
  ],
  usecase: [
    { label: '액터', type: 'box', subtype: 'actor', icon: '/assets/tool-icons/actor.svg' },
    { label: '유스케이스', type: 'box', subtype: 'usecase', icon: '/assets/tool-icons/usecase.svg' },
    { label: 'include', type: 'relationship', subtype: 'include', icon: '/assets/tool-icons/include.svg' },
    { label: 'extend', type: 'relationship', subtype: 'extend', icon: '/assets/tool-icons/extend.svg' }
  ]
}

const selectTool = (tool) => {
  toolStore.setSelectedTool(tool)
}

const isSelected = (tool) => {
  const selected = toolStore.selectedTool
  return selected && selected.type === tool.type && selected.subtype === tool.subtype
}

const onDragStart = (tool, event) => {
  event.dataTransfer.setData('application/json', JSON.stringify(tool))
}
</script>

<style scoped>
.toolbox {
  width: 200px;
  height: 92%;
  background-color: #f4f6f8;
  border-right: 1px solid #ccc;
  padding: 16px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.diagram-tabs {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto; /* 🔥 툴박스 하단으로 밀기 */
}

.tab-btn {
  padding: 10px;
  background-color: white;
  border: 1px solid #aaa;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  text-align: left;
}

.tab-btn.active {
  background-color: #007bff;
  color: white;
  border-color: #007bff;
}

.icon-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px;
  border: 1px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: 0.2s;
  width: 100px;
}

.icon-button:hover {
  background-color: #e8f0fe;
}

.icon-button.selected {
  border-color: #007bff;
  background-color: #dbefff;
}

.icon-image {
  width: 36px;
  height: 36px;
  margin-bottom: 4px;
}

.icon-label {
  font-size: 13px;
  text-align: center;
  color: #333;
}

</style>
