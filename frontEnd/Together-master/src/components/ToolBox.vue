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

      <!-- 클래스/ERD 다이어그램에서만 코드 변환 버튼 노출 -->
      <button
        v-if="currentDiagram === 'class' || currentDiagram === 'erd'"
        class="code-convert-btn"
        @click="showCodeModal = true"
      >
        <span class="code-icon">🧑‍💻</span>
        코드 변환
      </button>
    </div>

    <!-- 다이어그램 종류 선택 탭 (아래 고정) -->
    <div class="diagram-tabs">
      <button
        v-for="type in diagramTypes"
        :key="type.value"
        :class="['tab-btn', { active: currentDiagram === type.value }]"
        @click="onDiagramTabClick(type)"
      >
        {{ type.label }}
      </button>
    </div>

    <!-- 코드 변환 모달: class/erd 분기 -->
    <ClassCodeModal
      v-if="showCodeModal && currentDiagram === 'class'"
      :codeId="classCodeId"
      :codeName="classCodeName"
      @close="showCodeModal = false"
    />
    <ErdCodeModal
      v-if="showCodeModal && currentDiagram === 'erd'"
      :codeId="erdCodeId"
      :codeName="erdCodeName"
      @close="showCodeModal = false"
    />
  </div>
</template>

<script setup>
import { ref, watch, onMounted, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useToolStore } from '@/stores/toolStore'
import ClassCodeModal from '@/components/konva/ClassCodeModal.vue'
import ErdCodeModal from '@/components/konva/ErdCodeModal.vue'

const router = useRouter()
const route = useRoute()
const toolStore = useToolStore()
const showCodeModal = ref(false)
const currentDiagram = ref('class')

// 코드 모달용 ID/이름 (예시, 실제로는 페이지/상황에 맞게 동기화 필요)
const classCodeId = ref(null)
const classCodeName = ref('MyClass')
const erdCodeId = ref(null)
const erdCodeName = ref('MyERD')

// 다이어그램 종류 정의
const diagramTypes = [
  { label: '클래스', value: 'class', path: '/class-diagram' },
  { label: 'ERD', value: 'erd', path: '/erd-diagram' },
  { label: '정보구조도', value: 'info', path: '/info-structure' },
  { label: '유스케이스', value: 'usecase', path: '/usecase-diagram' }
]

// 경로 → 다이어그램 타입 변환
function getDiagramTypeByRoute(path) {
  if (path.startsWith('/class-diagram')) return 'class'
  if (path.startsWith('/erd-diagram')) return 'erd'
  if (path.startsWith('/info-structure')) return 'info'
  if (path.startsWith('/usecase-diagram')) return 'usecase'
  return 'class'
}

// 경로와 탭 싱크
const syncTabWithRoute = () => {
  const type = getDiagramTypeByRoute(route.path)
  currentDiagram.value = type
}
onMounted(syncTabWithRoute)
watch(() => route.path, () => nextTick(syncTabWithRoute))

// 탭 클릭 시 경로 이동
const onDiagramTabClick = (type) => {
  if (currentDiagram.value !== type.value) {
    currentDiagram.value = type.value
    router.push(type.path)
  }
}

// 각 다이어그램별 툴 버튼 정의
const toolButtons = {
  class: [
    {
      label: '클래스 박스',
      type: 'box',
      subtype: 'class',
      icon: new URL('@/assets/classbox.png', import.meta.url).href
    }
  ],
  erd: [
    {
      label: '테이블',
      type: 'box',
      subtype: 'table',
      icon: new URL('@/assets/table.png', import.meta.url).href
    }
  ],
  info: [
    {
      label: '페이지',
      type: 'box',
      subtype: 'page',
      icon: '/assets/tool-icons/page.svg'
    },
    {
      label: '링크',
      type: 'relationship',
      subtype: 'link',
      icon: '/assets/tool-icons/link.svg'
    }
  ],
  usecase: [
    {
      label: '액터',
      type: 'box',
      subtype: 'actor',
      icon: new URL('@/assets/actor.png', import.meta.url).href
    },
    {
      label: '유스케이스',
      type: 'box',
      subtype: 'usecase',
      icon: new URL('@/assets/circle.png', import.meta.url).href
    },
  ]
}

// 툴 선택/드래그
const selectTool = (tool) => toolStore.setSelectedTool(tool)
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
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: flex-start;
}
.diagram-tabs {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
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
.code-convert-btn {
  margin-top: 12px;
  grid-column: span 2;
  padding: 10px 0;
  background: linear-gradient(90deg, #3d5afe, #00bcd4 70%);
  color: #fff;
  border: none;
  border-radius: 8px;
  font-weight: bold;
  font-size: 16px;
  cursor: pointer;
  transition: 0.18s;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  box-shadow: 0 2px 8px #0001;
  width: 100%;
  margin-top: 300px
}
.code-convert-btn:hover {
  filter: brightness(1.08);
}
.code-icon {
  font-size: 18px;
}
</style>
