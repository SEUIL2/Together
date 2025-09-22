<template>
  <div class="toolbox">
    <div class="top-controls" v-if="showTopControls">
      <button
        class="btn-control"
        @click="showCodeModal = true"
      >
        <span class="btn-icon">🧑‍💻</span>
        코드 변환
      </button>
    </div>
    <!-- 아래 동일 -->
    <div
      class="icon-grid"
      :class="{ 'no-top-border': !showTopControls }"
    >
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

    <!-- 코드 변환 모달들 -->
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
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useToolStore } from '@/stores/toolStore.js'
import ClassCodeModal from '@/components/diagramtool/classdiagram/ClassCodeModal.vue'
import ErdCodeModal from '@/components/diagramtool/erd/ErdCodeModal.vue'

const route = useRoute()
const toolStore = useToolStore()
const showCodeModal = ref(false)

// 현재 다이어그램 타입 추출
const currentDiagram = computed(() => {
  if (route.path.startsWith('/class-diagram')) return 'class'
  if (route.path.startsWith('/erd-diagram')) return 'erd'
  if (route.path.startsWith('/info-structure')) return 'info'
  if (route.path.startsWith('/usecase-diagram')) return 'usecase'
  if (route.path.startsWith('/sequence-diagram')) return 'sequence'
  return 'class'
})

// Top-controls(코드 변환 등) 표시 여부
const showTopControls = computed(() => currentDiagram.value === 'class' || currentDiagram.value === 'erd')

// 코드 모달용 ID/이름 (상황 맞게 동기화)
const classCodeId = ref(null)
const classCodeName = ref('MyClass')
const erdCodeId = ref(null)
const erdCodeName = ref('MyERD')

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
      label: '박스',
      type: 'box',
      subtype: 'page',
      icon: new URL('@/assets/classbox.png', import.meta.url).href
    },
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
  ],
  sequence: [
    {
      label: '객체',
      type: 'box',
      subtype: 'lifeline',
      icon: new URL('@/assets/classbox.png', import.meta.url).href
    },
    {
      label: '액터',
      type: 'box',
      subtype: 'actor',
      icon: new URL('@/assets/actor.png', import.meta.url).href
    },
    {
      label: '반복(Loop)',
      type: 'fragment',
      subtype: 'loop',
      icon: new URL('@/assets/loop.png', import.meta.url).href
    },
    {
      label: '대안(Alt)',
      type: 'fragment',
      subtype: 'alt',
      icon: new URL('@/assets/alt.png', import.meta.url).href
    },
    // opt는 alt와 유사하여 아이콘 생략 가능
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
  position: absolute;
  top: 80px;
  right: 20px;
  z-index: 10;
  width: 150px;
  background: #fafbfc;
  border: 1.5px solid #e4e7ef;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  box-sizing: border-box;
  padding: 16px 14px;
  display: flex;
  flex-direction: column;
}

.top-controls {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 18px;
}

.btn-control {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  padding: 11px 0;
  background: #fff;
  color: #344052;
  border: 1.2px solid #d7dbe7;
  border-radius: 8px;
  font-weight: 600;
  font-size: 15px;
  cursor: pointer;
  transition: background 0.13s, border-color 0.15s;
  justify-content: center;
}
.btn-control:hover {
  background: #f3f7fb;
  border-color: #7a9ff8;
}
.btn-icon {
  font-size: 18px;
  margin-right: 2px;
  opacity: 0.75;
}

.icon-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  align-items: center;
  margin-top: 6px;
  padding-top: 8px;
  border-top: 1px solid #e9edf5;
}

.icon-grid.no-top-border {
  border-top: none;
  padding-top: 0;
  margin-top: 0;
}

.icon-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 8px 6px 8px;
  border: 1.2px solid #f3f4fa;
  border-radius: 9px;
  cursor: pointer;
  transition: background 0.16s, border-color 0.16s;
  width: 98px;
  background: #fcfdff;
}
.icon-button:hover {
  background: #eef4fa;
  border-color: #b3c7e6;
}
.icon-button.selected {
  border-color: #3574ef;
  background: #e8f0fe;
}
.icon-image {
  width: 36px;
  height: 36px;
  margin-bottom: 4px;
}
.icon-label {
  font-size: 13px;
  text-align: center;
  color: #23355b;
  font-weight: 500;
  letter-spacing: 0.01em;
}

@media (max-width: 900px) {
  .toolbox {
    width: 130px;
    min-width: 110px;
    padding: 8px 4px;
  }
  .icon-button {
    width: 60px;
    padding: 6px 1px;
  }
  .icon-image {
    width: 25px;
    height: 25px;
  }
  .icon-label {
    font-size: 11px;
  }
}

</style>