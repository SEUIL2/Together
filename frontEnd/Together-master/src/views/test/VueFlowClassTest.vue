<template>
  <div class="editor-layout">
    <DiagramTabs 
      :active-tab-id="activeTab"
      @tab-changed="onTabChange" 
    />

    <!-- 저장 상태 토스트 -->
    <div v-if="saveStatus !== 'idle'" class="save-toast" :class="saveStatus">
      {{ saveStatus === 'saving' ? '저장 중...' : saveStatus === 'saved' ? '💾 저장 완료' : '저장 실패!' }}
    </div>

    <div 
      class="canvas-wrapper" 
      ref="flowWrapper" 
      @dragover="onDragOver" 
      @click="hideContextMenu"
      @drop="onDrop"
    >
      
      <!-- ✅ [수정] marker 정의를 VueFlow 내부로 이동 -->
      <VueFlow
        :key="activeTab"
        ref="vueFlowRef"
        v-model:nodes="activeNodes"
        v-model:edges="activeEdges"
        :node-types="nodeTypes"
        :edge-types="edgeTypes"
        edges-up-front
        @connect="onConnect" 
        @nodes-drag-stop="onNodeDragStop" 
        @node-context-menu="onNodeContextMenu"
        @edge-context-menu="onEdgeContextMenu"
        @move-start="hideContextMenu"
        class="vue-flow-wrapper"
      >
        <Background />
        <Controls />

        <!-- [수정] SVG 정의를 VueFlow 컴포넌트의 기본 슬롯 안으로 이동 -->
        <defs>
          <marker
            id="arrow-closed"
            viewBox="-10 -5 10 10"
            refX="0"
            refY="0"
            markerUnits="strokeWidth"
            markerWidth="12"
            markerHeight="12"
            
            orient="auto"
          >
            <!-- [수정] 화살표가 올바른 방향을 향하도록 path를 수정합니다. -->
            <path d="M 0 0 L -10 -5 L -10 5 z" fill="#555" />
          </marker>
        </defs>
      </VueFlow>

      <Toolbox />

      <!-- 우클릭 컨텍스트 메뉴 -->
      <div
        v-if="contextMenu.visible" 
        class="context-menu" 
        :style="{ top: `${contextMenu.y}px`, left: `${contextMenu.x}px` }"
        @click.stop
      >
        <!-- 관계선(Edge)을 위한 상세 메뉴 -->
        <div v-if="contextMenu.target?.type === 'edge'">
          <div class="menu-section">
            <div class="menu-label">선 스타일</div>
            <select class="menu-select" @change="setEdgeStyle($event.target.value)" :value="getEdgeProperty('lineStyle')">
              <option value="none">실선</option>
              <option value="dashed">점선</option>
            </select>
          </div>
          <!-- [수정] 시작 화살표 설정 메뉴 -->
          <div class="menu-section">
            <div class="menu-label">시작 화살표</div>
            <select class="menu-select" @change="setEdgeMarkerStart($event.target.value)" :value="getEdgeProperty('markerStart')">
              <option value="">없음</option>
              <option value="url(#arrow-closed)">일반화 (삼각형)</option>
            </select>
          </div>
          <!-- [수정] 끝 화살표 설정 메뉴 -->
          <div class="menu-section">
            <div class="menu-label">끝 화살표</div>
            <select class="menu-select" @change="setEdgeMarkerEnd($event.target.value)" :value="getEdgeProperty('markerEnd')">
              <option value="">없음</option>
              <option value="url(#arrow-closed)">일반화 (삼각형)</option>
            </select>
          </div>
          <div class="menu-section">
            <div class="menu-label">라벨</div>
            <button class="menu-item" @click="setEdgeLabel('<<include>>')">&lt;&lt;include&gt;&gt;</button>
            <button class="menu-item" @click="setEdgeLabel('<<extend>>')">&lt;&lt;extend&gt;&gt;</button>
            <button class="menu-item" @click="setEdgeLabel('')">라벨 삭제</button>
          </div>
        </div>
        <!-- 삭제 버튼 -->
        <div class="menu-section danger-zone">
          <div class="menu-item danger" @click="deleteContextMenuTarget">
            <span class="icon">🗑️</span> 삭제
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, computed, ref as vueRef, markRaw, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '@/api'
import { debounce } from 'lodash'
// [수정] 중복된 import를 모두 정리하고, 필요한 모든 것을 한 번에 가져옵니다.
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

import CustomClassNode from '@/components/diagramtool/vueflow/CustomClassNode.vue'
import CustomActorNode from '@/components/diagramtool/vueflow/Usecase/CustomActorNode.vue'
import CustomUsecaseNode from '@/components/diagramtool/vueflow/Usecase/CustomUsecaseNode.vue'
import CustomSystemNode from '@/components/diagramtool/vueflow/Usecase/CustomSystemNode.vue'
import CustomNoteNode from '@/components/diagramtool/vueflow/Usecase/CustomNoteNode.vue'
import CustomUsecaseEdge from '@/components/diagramtool/vueflow/Usecase/CustomUsecaseEdge.vue'

import Toolbox from '@/components/diagramtool/vueflow/Toolbox.vue' 
import DiagramTabs from '@/components/diagramtool/vueflow/DiagramTabs.vue' 

const nodeTypes = {
  classNode: markRaw(CustomClassNode),
  'usecase-actor': markRaw(CustomActorNode),
  'usecase-bubble': markRaw(CustomUsecaseNode),
  // [수정] 중복 정의를 모두 제거하고, markRaw를 적용하지 않아 크기 조절이 가능하도록 합니다.
  'usecase-system': CustomSystemNode, // markRaw를 제거하여 크기 조절이 가능하도록 합니다.
  note: markRaw(CustomNoteNode),
};
// [수정] 디버깅용 코드를 제거하고, 커스텀 엣지를 사용하도록 다시 설정합니다.
// (라벨 위치 조절 등 커스텀 기능이 필요하기 때문)
const edgeTypes = {
  'usecase-edge': markRaw(CustomUsecaseEdge),
};

const newNodeId = ref(10) 
const flowWrapper = vueRef(null);
// 🚀 [수정] useVueFlow 훅 대신 표준 ref를 사용하여 VueFlow 컴포넌트 인스턴스를 직접 참조합니다.
// 이렇게 하면 컴포넌트 생명주기 문제를 피할 수 있습니다.
const vueFlowRef = ref(null);
const { project } = useVueFlow();
const route = useRoute();
const activeTab = ref('usecase'); 

const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  target: null,
});

const allDiagramData = ref({
  class: {
    nodes: [
      { id: '1', type: 'classNode', position: { x: 50, y: 50 }, data: { label: 'User' } },
      { id: '2', type: 'classNode', position: { x: 350, y: 150 }, data: { label: 'Admin' } },
    ],
    edges: [
      { id: 'e1-2', source: '2', target: '1', type: 'default', sourceHandle: 'top', targetHandle: 'bottom' },
    ],
  },
  usecase: { nodes: [], edges: [] },
  sequence: { nodes: [], edges: [] },
  erd: { nodes: [], edges: [] },
  infostructure: { nodes: [], edges: [] },
});

const activeNodes = computed({
  get: () => allDiagramData.value[activeTab.value]?.nodes || [],
  set: (newNodes) => {
    if (allDiagramData.value[activeTab.value]) {
      allDiagramData.value[activeTab.value].nodes = newNodes;
    }
  }
});
const activeEdges = computed({
  get: () => allDiagramData.value[activeTab.value]?.edges || [],
  set: (newEdges) => {
    if (allDiagramData.value[activeTab.value]) {
      allDiagramData.value[activeTab.value].edges = newEdges;
    }
  }
});

function onTabChange(tabId) {
  activeTab.value = tabId;
}

function onConnect(connectionParams) {
  const newEdge = {
    id: `e${connectionParams.source}${connectionParams.sourceHandle}-${connectionParams.target}${connectionParams.targetHandle}-${Date.now()}`,
    source: connectionParams.source,
    target: connectionParams.target,
    type: 'usecase-edge', // [수정] 커스텀 라벨 위치 등을 위해 커스텀 엣지를 사용합니다.
    sourceHandle: connectionParams.sourceHandle,
    targetHandle: connectionParams.targetHandle,
    data: { 
      label: '',
      lineStyle: 'none',
      labelOffsetX: 0,
      labelOffsetY: -20,
    },
    markerStart: '', // [추가] 시작 화살표 기본값
    markerEnd: 'url(#arrow-closed)',   // [수정] 연결 시 기본으로 끝 화살표를 표시합니다.
  }
  activeEdges.value = [...activeEdges.value, newEdge];
}

function getEdgeProperty(key) {
  if (contextMenu.value.target?.type !== 'edge') return null;
  const edge = activeEdges.value.find(e => e.id === contextMenu.value.target.id);
  if (!edge) return null;
  // [수정] 'markerStart'와 'markerEnd' 속성을 올바르게 가져오도록 수정합니다.
  if (key === 'markerEnd') return edge.markerEnd || '';
  if (key === 'markerStart') return edge.markerStart || '';
  
  return edge.data ? edge.data[key] : null;
}

function onNodeContextMenu(event) {
  const wrapperBounds = flowWrapper.value?.getBoundingClientRect();
  if (!wrapperBounds) return;
  event.event.preventDefault();
  
  contextMenu.value = {
    visible: true,
    x: event.event.clientX - wrapperBounds.left,
    y: event.event.clientY - wrapperBounds.top,
    target: { type: 'node', id: event.node.id },
  };
}

function onEdgeContextMenu(event) {
  const wrapperBounds = flowWrapper.value?.getBoundingClientRect();
  if (!wrapperBounds) return;
  event.event.preventDefault();

  contextMenu.value = {
    visible: true,
    x: event.event.clientX - wrapperBounds.left,
    y: event.event.clientY - wrapperBounds.top,
    target: { type: 'edge', id: event.edge.id },
  };
}

function hideContextMenu() {
  if (vueFlowRef.value) vueFlowRef.value.wrapperRef?.focus();
  if (!contextMenu.value.visible) return;
  contextMenu.value.visible = false;
}

function deleteContextMenuTarget() {
  const { type, id } = contextMenu.value.target;
  if (!type || !id) return;

  if (type === 'node') {
    activeNodes.value = activeNodes.value.filter(node => node.id !== id);
    activeEdges.value = activeEdges.value.filter(edge => edge.source !== id && edge.target !== id);
  } else if (type === 'edge') {
    activeEdges.value = activeEdges.value.filter(edge => edge.id !== id);
  }

  hideContextMenu();
}

function setEdgeStyle(lineStyle) {
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    // [수정] 기본 엣지가 인식하도록 최상위 style 속성을 직접 변경합니다.
    const newStyle = {
      ...updatedEdges[edgeIndex].style,
      strokeDasharray: lineStyle === 'dashed' ? '5 5' : undefined,
    };
    updatedEdges[edgeIndex] = { ...updatedEdges[edgeIndex], style: newStyle };
    activeEdges.value = updatedEdges;
    // data.lineStyle도 함께 업데이트하여 메뉴 상태를 유지합니다.
    updatedEdges[edgeIndex].data.lineStyle = lineStyle;
  }
}

function setEdgeMarkerStart(markerId) {
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    // [수정] Vue 반응성을 확실히 트리거하기 위해 객체를 새로 생성합니다.
    const edgeToUpdate = { ...updatedEdges[edgeIndex], markerStart: markerId };
    updatedEdges[edgeIndex] = edgeToUpdate;
    activeEdges.value = updatedEdges;
  }
}

function setEdgeMarkerEnd(markerId) {
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    // [수정] Vue 반응성을 확실히 트리거하기 위해 객체를 새로 생성합니다.
    const edgeToUpdate = { ...updatedEdges[edgeIndex], markerEnd: markerId };
    updatedEdges[edgeIndex] = edgeToUpdate;
    activeEdges.value = updatedEdges;
  }
}

function setEdgeLabel(label) {
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    // [수정] 기본 엣지가 인식하도록 최상위 label 속성을 직접 변경하고, data.label도 함께 업데이트합니다.
    const edgeToUpdate = { ...updatedEdges[edgeIndex] };
    edgeToUpdate.data = { ...edgeToUpdate.data, label: label };
    updatedEdges[edgeIndex] = edgeToUpdate;
    activeEdges.value = updatedEdges;
  }
  hideContextMenu();
}

function onDragOver(event) {
  event.preventDefault(); 
  if (event.dataTransfer) event.dataTransfer.dropEffect = 'copy';
}

function onDrop(event) {
  event.preventDefault();
  const nodeType = event.dataTransfer?.getData('application/node');
  if (!nodeType) return; 
  const wrapperBounds = flowWrapper.value?.getBoundingClientRect();
  if (!wrapperBounds) return;

  // [수정] 화면 좌표를 Vue Flow의 내부 좌표로 변환합니다.
  const projectedPosition = project({
    x: event.clientX - wrapperBounds.left,
    y: event.clientY - wrapperBounds.top,
  });

  let label = '새 항목';
  switch (nodeType) {
    case 'usecase-actor': label = '액터'; break;
    case 'usecase-bubble': label = '유스케이스'; break;
    case 'usecase-system': label = '시스템 경계'; break;
    case 'note': label = '노트...'; break;
    case 'classNode': label = 'NewClass'; break;
    case 'entityNode': label = 'NewEntity'; break;
  }

  const newNode = {
    id: `node-${newNodeId.value++}`,
    type: nodeType,
    position: projectedPosition, // 변환된 좌표를 사용합니다.
    data: { label },
    // [추가] 시스템 경계 노드에 초기 크기를 지정합니다.
    ...(nodeType === 'usecase-system' && {
      style: {
        width: '400px',
        height: '300px',
      },
    }),
  };

  allDiagramData.value[activeTab.value].nodes.push(newNode);
}

function onNodeDragStop() {}

// === 저장 관련 ===
const saveStatus = ref('idle')

const saveUsecase = debounce(async () => {
  const readonly = route.query.readonly === 'true'
  if (readonly) {
    console.log('🔒 읽기 전용 모드입니다. 저장하지 않습니다.')
    return
  }

  saveStatus.value = 'saving'

  // VueFlow 인스턴스에서 toObject() 메서드를 사용하여 현재 상태를 가져옵니다.
  const flowData = vueFlowRef.value?.toObject();
  if (!flowData) {
    console.error('Flow 데이터를 가져올 수 없습니다.');
    saveStatus.value = 'error';
    return;
  }

  const jsonData = {
    nodes: flowData.nodes,
    edges: flowData.edges,
    // 필요하다면 viewport 정보도 저장할 수 있습니다.
    // viewport: flowData.viewport,
  }

  const formData = new FormData()
  formData.append('type', 'usecase') // 현재는 유스케이스만 저장
  formData.append('json', JSON.stringify(jsonData))
  formData.append('projectId', route.params.projectId);

  try {
    await api.post('/design/upload', formData);
    saveStatus.value = 'saved'
    setTimeout(() => saveStatus.value = 'idle', 1200)
    console.log('✅ 유스케이스 다이어그램 저장 성공')
  } catch (err) {
    console.error('❌ 유스케이스 저장 실패:', err)
    saveStatus.value = 'error'
    setTimeout(() => saveStatus.value = 'idle', 3000)
    alert('⚠️ 유스케이스 저장 중 오류 발생')
  }
}, 1000)

watch([activeNodes, activeEdges], saveUsecase, { deep: true })

// === 불러오기 ===
onMounted(async () => {
  try {
    const res = await api.get('/design/all', {
      params: { projectId: route.params.projectId }
    })

    const { usecase } = res.data
    if (usecase?.json) {
      const parsed = JSON.parse(usecase.json)
      allDiagramData.value.usecase = { nodes: parsed.nodes || [], edges: parsed.edges || [] };
      console.log('✅ 유스케이스 불러오기 성공:', parsed)
    }
  } catch (err) {
    console.error('❌ 유스케이스 초기 데이터 로드 실패:', err)
  }
})
</script>

<style scoped>
.editor-layout {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: calc(100vh - 70px); 
  overflow: hidden;
  background-color: #f8f9fa;
}
.canvas-wrapper {
  flex-grow: 1;
  position: relative;
  overflow: hidden;
}
.vue-flow-wrapper {
  width: 100%;
  height: 100%;
}
</style>

<style>
.context-menu {
  position: absolute;
  z-index: 1000;
  min-width: 200px;
  background: #fff;
  border-radius: 11px;
  box-shadow: 0 4px 24px 0 #2230462a;
  border: 1.5px solid #e8eaf0;
  padding: 8px 6px;
  animation: pop-in 0.12s cubic-bezier(.41,.84,.67,1.2);
  user-select: none;
}
@keyframes pop-in {
  0% { transform: scale(0.95); opacity: 0.5; }
  100% { transform: scale(1); opacity: 1; }
}
.menu-section {
  padding: 4px;
  margin-bottom: 4px;
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
.menu-item {
  display: flex;
  align-items: center;
  font-size: 15px;
  border-radius: 7px;
  padding: 8px 10px;
  color: #2c3e50;
  cursor: pointer;
  transition: background 0.15s;
  background: none;
  border: none;
  width: 100%;
  text-align: left;
}
.menu-item .icon {
  font-size: 17px;
  margin-right: 8px;
}
.menu-item:hover {
  background: #f1f5f9;
}
.danger-zone {
  margin-top: 4px;
  border-top: 1px solid #e2e8f0;
  padding-top: 4px;
}
.menu-item.danger {
  color: #e44e5c;
  font-weight: 500;
}
.menu-item.danger:hover {
  background: #ffe6e7;
  color: #d7263d;
}
.save-toast {
  position: fixed;
  bottom: 20px;
  right: 20px;
  padding: 10px 20px;
  border-radius: 8px;
  color: white;
  font-weight: 600;
  z-index: 1000;
  transition: opacity 0.3s;
}
.save-toast.saving { background-color: #777; }
.save-toast.saved { background-color: #323232; }
.save-toast.error { background-color: #dc3545; }
</style>
