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
            viewBox="0 0 10 10" 
            refX="10" 
            refY="5"
            markerWidth="8"
            markerHeight="8"
            orient="auto"
          >
            <path d="M 0 0 L 10 5 L 0 10 z" fill="#000000" />
          </marker>

          </defs>
      </VueFlow>
      <!-- activeTab을 Toolbox에 prop으로 전달하여 현재 활성화된 다이어그램에 맞는 도구만 표시하도록 합니다. -->
      <Toolbox :active-tab="activeTab" />

      <!-- 우클릭 컨텍스트 메뉴 -->
      <div
        v-if="contextMenu.visible" 
        class="context-menu" 
        :style="{ top: `${contextMenu.y}px`, left: `${contextMenu.x}px` }"
        @click.stop
      >
        <!-- 관계선(Edge)을 위한 상세 메뉴 -->
        <div v-if="contextMenu.target?.type === 'edge' && currentContextMenuTargetEdge">
          <div class="menu-section">
            <div class="menu-label">선 스타일</div>
            <select class="menu-select" @change="setEdgeStyle($event.target.value)" :value="currentContextMenuTargetEdge.data?.lineStyle || 'none'">
              <option value="none">실선</option>
              <option value="dashed">점선</option>
            </select>
          </div>

          <div class="menu-section">
            <div class="menu-label">선 종류</div>
            <select class="menu-select" @change="setEdgeType($event.target.value)" :value="currentContextMenuTargetEdge.type || 'step'">
              <option value="step">직각 선</option>
              <option value="smoothstep">곡선</option>
              <option value="default">직선</option>
            </select>
          </div>

          <!-- 유스케이스 다이어그램 관계선 메뉴 -->
        <template v-if="activeTab === 'usecase'">
          <div class="menu-section">
            <div class="menu-label">시작 화살표</div>
            <select class="menu-select" @change="setEdgeMarkerEnd($event.target.value)" :value="currentContextMenuTargetEdge.markerEnd?.type || ''">
              <option value="">없음</option>
              <option value="arrowclosed">일반화 (삼각형)</option>
            </select>
          </div>
          <div class="menu-section">
            <div class="menu-label">끝 화살표</div>
            <select class="menu-select" @change="setEdgeMarkerStart($event.target.value)" :value="currentContextMenuTargetEdge.markerStart?.type || ''">
              <option value="">없음</option>
              <option value="arrowclosed">일반화 (삼각형)</option>
            </select>
          </div>
          <div class="menu-section">
            <div class="menu-label">라벨</div>
            <button class="menu-item" @click="setEdgeLabel('<<include>>')">&lt;&lt;include&gt;&gt;</button>
            <button class="menu-item" @click="setEdgeLabel('<<extend>>')">&lt;&lt;extend&gt;&gt;</button>
            <button class="menu-item" @click="setEdgeLabel('')">라벨 삭제</button>
          </div>
        </template>

          <!-- 클래스 다이어그램 관계선 메뉴 -->
          <template v-else-if="activeTab === 'classDiagram'">
            <div class="menu-section">
              <div class="menu-label">관계 종류</div>
              <select class="menu-select" @change="setEdgeRelationshipType($event.target.value)" :value="currentContextMenuTargetEdge.data?.relationshipType || 'association'">
                <option value="association">연관 (실선)</option>
                <option value="generalization">일반화 (빈 삼각형)</option>
                <option value="realization">구현 (점선 빈 삼각형)</option>
                <option value="aggregation">집합 (빈 마름모)</option>
                <option value="composition">복합 (채워진 마름모)</option>
                <option value="dependency">의존 (점선 화살표)</option>
              </select>
            </div>
            <div class="menu-section">
              <div class="menu-label">라벨</div>
              <button class="menu-item" @click="setEdgeLabel('1..*')">1..*</button>
              <button class="menu-item" @click="setEdgeLabel('0..1')">0..1</button>
              <button class="menu-item" @click="setEdgeLabel('')">라벨 삭제</button>
            </div>
          </template>

          <!-- ERD 관계선 메뉴 -->
          <template v-else-if="activeTab === 'erd'">
            <div class="menu-section">
              <div class="menu-label">시작 관계</div>
              <select class="menu-select" @change="setEdgeMarkerStart($event.target.value)" :value="currentContextMenuTargetEdge.markerStart || ''">
                <option value="">없음</option>
                <option value="url(#erd-one-to-one)">1</option>
                <option value="url(#erd-one-to-many)">1:N</option>
                <option value="url(#erd-many-to-many)">N:M</option>
              </select>
            </div>
            <div class="menu-section">
              <div class="menu-label">끝 관계</div>
              <select class="menu-select" @change="setEdgeMarkerEnd($event.target.value)" :value="currentContextMenuTargetEdge.markerEnd || ''">
                <option value="">없음</option>
                <option value="url(#erd-one-to-one)">1</option>
                <option value="url(#erd-one-to-many)">1:N</option>
                <option value="url(#erd-many-to-many)">N:M</option>
              </select>
            </div>
          </template>

          <!-- 정보구조도 관계선 메뉴 (기본 선 스타일만) -->
         <template v-else-if="activeTab === 'infostructure'">
            <div class="menu-section">
              <div class="menu-label">시작 화살표</div>
              <select class="menu-select" @change="setEdgeMarkerStart($event.target.value)" :value="currentContextMenuTargetEdge.markerStart?.type || ''">
                <option value="">없음</option>
                <option value="arrowclosed">채워진 화살표</option>
              </select>
            </div>
            <div class="menu-section">
              <div class="menu-label">끝 화살표</div>
              <select class="menu-select" @change="setEdgeMarkerEnd($event.target.value)" :value="currentContextMenuTargetEdge.markerEnd?.type || ''">
                <option value="">없음</option>
                <option value="arrowclosed">채워진 화살표</option>
              </select>
            </div>
          </template>
        </div>

        <div v-else-if="contextMenu.target?.type === 'node' && activeTab === 'infostructure'">
          <div class="menu-section">
            <div class="menu-label">페이지 헤더 색상</div>
            <div class="color-palette">
              <button class="color-swatch" style="background: #718096;" @click="setNodeHeaderColor('#718096')"></button>
              <button class="color-swatch" style="background: #63B3ED;" @click="setNodeHeaderColor('#63B3ED')"></button>
              <button class="color-swatch" style="background: #F6E05E;" @click="setNodeHeaderColor('#F6E05E')"></button>
              <button class="color-swatch" style="background: #68D391;" @click="setNodeHeaderColor('#68D391')"></button>
              <button class="color-swatch" style="background: #F56565;" @click="setNodeHeaderColor('#F56565')"></button>
            </div>
          </div>
        </div>

        <div v-else-if="contextMenu.target?.type === 'node'">
          <!-- 노드별 추가 메뉴는 추후 구현 -->
          <div class="menu-section">
            <div class="menu-label">노드 옵션</div>
            <!-- 노드별 메뉴는 여기에 추가될 수 있습니다. (예: 속성/메서드 추가) -->
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
import { VueFlow, useVueFlow, MarkerType } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

import CustomClassNode from '@/components/diagramtool/vueflow/CustomClassNode.vue'
import CustomActorNode from '@/components/diagramtool/vueflow/Usecase/CustomActorNode.vue'
import CustomUsecaseNode from '@/components/diagramtool/vueflow/Usecase/CustomUsecaseNode.vue'
import CustomSystemNode from '@/components/diagramtool/vueflow/Usecase/CustomSystemNode.vue'
import CustomNoteNode from '@/components/diagramtool/vueflow/Usecase/CustomNoteNode.vue'
import CustomPageNode from './CustomPageNode.vue'
import Toolbox from '@/components/diagramtool/vueflow/Toolbox.vue' 
import DiagramTabs from '@/components/diagramtool/vueflow/DiagramTabs.vue' 


const nodeTypes = {
  classNode: markRaw(CustomClassNode),
  interfaceNode: markRaw(CustomClassNode), // Placeholder for custom interface node
  enumNode: markRaw(CustomClassNode), // Placeholder for custom enum node
  packageNode: markRaw(CustomClassNode), // Placeholder for custom package node
  'usecase-actor': markRaw(CustomActorNode),
  'usecase-bubble': markRaw(CustomUsecaseNode),
  // [수정] 중복 정의를 모두 제거하고, markRaw를 적용하지 않아 크기 조절이 가능하도록 합니다.
  'usecase-system': CustomSystemNode, // markRaw를 제거하여 크기 조절이 가능하도록 합니다.
  note: markRaw(CustomNoteNode),
  entityNode: markRaw(CustomClassNode), // ERD 및 정보구조도 노드 타입 추가 (임시로 CustomClassNode 사용)
  relationshipNode: markRaw(CustomClassNode),
  attributeNode: markRaw(CustomClassNode),
  pageNode: markRaw(CustomPageNode),
};
// [수정] 디버깅용 코드를 제거하고, 커스텀 엣지를 사용하도록 다시 설정합니다.
// (라벨 위치 조절 등 커스텀 기능이 필요하기 때문).
// pageNode는 CustomClassNode를 임시로 사용합니다.
const edgeTypes = {
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
  classDiagram: {
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

// 현재 컨텍스트 메뉴의 대상 노드/엣지를 가져오는 computed 속성
const currentContextMenuTargetNode = computed(() => {
  if (contextMenu.value.target?.type === 'node') {
    return activeNodes.value.find(node => node.id === contextMenu.value.target.id);
  }
  return null;
});

const currentContextMenuTargetEdge = computed(() => {
  if (contextMenu.value.target?.type === 'edge') {
    return activeEdges.value.find(edge => edge.id === contextMenu.value.target.id);
  }
  return null;
});

function onConnect(connectionParams) {
  let edgeType = 'default'; // 기본 엣지 타입
  let markerEnd = undefined;
  let markerStart = undefined;

  if (activeTab.value === 'usecase') {
    edgeType = 'usecase-edge';
    
  } else if (activeTab.value === 'classDiagram') {
    // 클래스 다이어그램은 기본적으로 연관 관계 (화살표 없음)
    edgeType = 'default';
  } else if (activeTab.value === 'erd') {
    // ERD는 기본적으로 화살표 없음 (관계선은 나중에 커스텀 마커로 표현)
    edgeType = 'default';
  } else if (activeTab.value === 'infostructure' ) {
    // 정보구조도 및 시퀀스 다이어그램도 기본 엣지 타입 사용
    edgeType = 'default';
  }

  const newEdge = {
    id: `e${connectionParams.source}${connectionParams.sourceHandle}-${connectionParams.target}${connectionParams.targetHandle}-${Date.now()}`,
    source: connectionParams.source,
    target: connectionParams.target,
    type: edgeType,
    sourceHandle: connectionParams.sourceHandle,
    targetHandle: connectionParams.targetHandle,
    data: { 
      label: '',
      lineStyle: 'none',
      labelOffsetX: 0,
      labelOffsetY: -20,
    },
    markerStart: markerStart,
    markerEnd: markerEnd,

    // [수정] 기본 선이 읽을 수 있도록 최상위 속성 추가
    label: '', // 라벨 텍스트
    labelStyle: { fill: '#2d3748', fontWeight: 500 }, // 라벨 텍스트 스타일
    labelBgStyle: { fill: '#f8f9fa' }, // 라벨 배경색 (캔버스 배경과 동일하게)
    labelBgPadding: [4, 8], // 라벨 배경 여백
    labelBgBorderRadius: 4, // 라벨 배경 둥근 모서리
    // [추가] 라벨을 선 위쪽으로 이동시킵니다.
    labelYOffset: -20, // 원하는 만큼 숫자를 조절해보세요.
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

// [추가] 선의 타입을 변경하는 함수 (step, smoothstep, default)
function setEdgeType(type) {
  const { id } = contextMenu.value.target;
  if (!id) return; // 대상 ID가 없으면 중단

  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    // 반응성을 위해 새 객체를 만들고 'type' 속성을 변경
    const edgeToUpdate = { ...updatedEdges[edgeIndex], type: type };
    updatedEdges[edgeIndex] = edgeToUpdate;
    activeEdges.value = updatedEdges;
  }
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

function setEdgeMarkerStart(markerValue) { // markerId -> markerValue
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];

    // [수정] 값에 따라 내장 마커 객체 또는 undefined를 할당합니다.
    let newMarkerStart;
    if (markerValue === 'arrowclosed') {
      newMarkerStart = { 
        type: MarkerType.ArrowClosed, 
        color: '#000000', 
        width: 15,
        height: 15,
      };
    } else {
      newMarkerStart = undefined; // '없음' 선택 시
    }

    const edgeToUpdate = { ...updatedEdges[edgeIndex], markerStart: newMarkerStart };
    updatedEdges[edgeIndex] = edgeToUpdate;
    activeEdges.value = updatedEdges;
  }
}

function setEdgeMarkerEnd(markerValue) { // markerId -> markerValue로 이름 변경
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    
    // [수정] 값에 따라 내장 마커 객체 또는 undefined를 할당합니다.
    let newMarkerEnd;
    if (markerValue === 'arrowclosed') {
      newMarkerEnd = { 
        type: MarkerType.ArrowClosed, // 우리가 import한 MarkerType 사용
        color: '#000000', // 검은색 지정
        width: 15,
        height: 15,
      };
    } else {
      newMarkerEnd = undefined; // '없음' 선택 시
    }

    const edgeToUpdate = { ...updatedEdges[edgeIndex], markerEnd: newMarkerEnd };
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
    
    // [수정] 반응성을 위해 새 객체로 복사
    const edgeToUpdate = { ...updatedEdges[edgeIndex] };

    // [수정] data.label과 함께 최상위 'label' 속성도 업데이트합니다.
    // 이것이 기본 선(default edge)이 읽는 값입니다.
    edgeToUpdate.data = { ...edgeToUpdate.data, label: label };
    edgeToUpdate.label = label; // <-- ★★★ 이 부분이 핵심입니다 ★★★

    updatedEdges[edgeIndex] = edgeToUpdate;
    activeEdges.value = updatedEdges;
  }
  hideContextMenu();
}

// [추가] 노드 헤더 색상을 변경하는 함수
function setNodeHeaderColor(color) {
  const { id } = contextMenu.value.target;
  if (!id) return; // 대상 ID가 없으면 중단

  // activeNodes에서 해당 노드를 찾습니다.
  const node = activeNodes.value.find(n => n.id === id);
  if (node) {
    // data 객체를 새로 만들어 반응성을 유지하며 headerColor를 업데이트합니다.
    node.data = { ...node.data, headerColor: color };
  }
  
  hideContextMenu(); // 메뉴 닫기
}

// 클래스 다이어그램 관계 종류 설정
function setEdgeRelationshipType(relationshipType) {
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    const edgeToUpdate = { ...updatedEdges[edgeIndex] };

    // 관계 종류에 따라 markerStart, markerEnd, style, label 변경
    edgeToUpdate.data = { ...edgeToUpdate.data, relationshipType: relationshipType };
    edgeToUpdate.data.lineStyle = 'none'; // 기본 실선
    edgeToUpdate.style = { strokeDasharray: undefined }; // 기본 실선
    edgeToUpdate.markerStart = '';
    edgeToUpdate.markerEnd = '';
    edgeToUpdate.data.label = '';

    switch (relationshipType) {
      case 'association':
        // 기본값 (화살표 없음)
        break;
      case 'generalization': // 일반화 (상속)
        edgeToUpdate.markerEnd = 'url(#arrow-closed)'; // 빈 삼각형
        break;
      case 'realization': // 구현 (점선 빈 삼각형)
        edgeToUpdate.data.lineStyle = 'dashed';
        edgeToUpdate.style = { strokeDasharray: '5 5' };
        edgeToUpdate.markerEnd = 'url(#arrow-closed)'; // 빈 삼각형
        break;
      case 'aggregation': // 집합 (빈 마름모)
        // TODO: 마름모 마커 정의 필요
        // edgeToUpdate.markerEnd = 'url(#diamond-open)';
        break;
      case 'composition': // 복합 (채워진 마름모)
        // TODO: 채워진 마름모 마커 정의 필요
        // edgeToUpdate.markerEnd = 'url(#diamond-filled)';
        break;
      case 'dependency': // 의존 (점선 화살표)
        edgeToUpdate.data.lineStyle = 'dashed';
        edgeToUpdate.style = { strokeDasharray: '5 5' };
        edgeToUpdate.markerEnd = 'url(#arrow-closed)'; // 일반 화살표
        break;
      // ERD 관계는 onConnect에서 처리하거나 별도 함수로 분리
      case 'erd-one-to-one':
        edgeToUpdate.markerStart = 'url(#erd-one-to-one)';
        edgeToUpdate.markerEnd = 'url(#erd-one-to-one)';
        break;
      case 'erd-one-to-many':
        edgeToUpdate.markerStart = 'url(#erd-one-to-one)';
        edgeToUpdate.markerEnd = 'url(#erd-one-to-many)';
        break;
      case 'erd-many-to-many':
        edgeToUpdate.markerStart = 'url(#erd-many-to-many)';
        edgeToUpdate.markerEnd = 'url(#erd-many-to-many)';
        break;
    }
    updatedEdges[edgeIndex] = edgeToUpdate;
    activeEdges.value = updatedEdges;
  }
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

  let newNode;
  let defaultWidth = 160;
  let defaultHeight = 100;

  switch (nodeType) {
    case 'classNode':
      newNode = {
        id: `node-${newNodeId.value++}`,
        type: 'classNode',
        position: projectedPosition,
        data: { 
          label: 'NewClass', 
          attributes: ['+ attribute: Type'], 
          methods: ['+ method(): ReturnType'] 
        },
        style: { width: defaultWidth, height: defaultHeight },
      };
      break;
    case 'interfaceNode':
      newNode = {
        id: `node-${newNodeId.value++}`,
        type: 'interfaceNode',
        position: projectedPosition,
        data: { 
          label: '<<interface>>\nNewInterface', 
          attributes: [], 
          methods: ['+ operation(): ReturnType'] 
        },
        style: { width: defaultWidth, height: defaultHeight },
      };
      break;
    case 'enumNode':
      newNode = {
        id: `node-${newNodeId.value++}`,
        type: 'enumNode',
        position: projectedPosition,
        data: { 
          label: '<<enum>>\nNewEnum', 
          attributes: ['VALUE1', 'VALUE2'], 
          methods: [] 
        },
        style: { width: defaultWidth, height: defaultHeight },
      };
      break;
    case 'packageNode':
      newNode = {
        id: `node-${newNodeId.value++}`,
        type: 'packageNode',
        position: projectedPosition,
        data: { label: 'NewPackage' },
        style: { width: 200, height: 150, backgroundColor: '#f0f8ff', border: '1px dashed #ccc' },
      };
      break;
    case 'note': // Generic note, used in Usecase and Class diagrams
      newNode = {
        id: `node-${newNodeId.value++}`,
        type: 'note',
        position: projectedPosition,
        data: { label: '노트...' },
        style: { width: 150, height: 80, backgroundColor: '#fffacd', border: '1px solid #e0e0e0' },
      };
      break;
    case 'usecase-actor':
      newNode = { id: `node-${newNodeId.value++}`, type: nodeType, position: projectedPosition, data: { label: '액터' } };
      break;
    case 'usecase-bubble':
      newNode = { id: `node-${newNodeId.value++}`, type: nodeType, position: projectedPosition, data: { label: '유스케이스' } };
      break;
    case 'usecase-system':
      newNode = {
        id: `node-${newNodeId.value++}`,
        type: nodeType,
        position: projectedPosition,
        data: { label: '시스템 경계' },
        style: { width: '400px', height: '300px' },
      };
      break; // 쉼표를 세미콜론으로 수정하여 문법 오류 해결
    case 'entityNode': // ERD entity (테이블)
      newNode = { id: `node-${newNodeId.value++}`, type: nodeType, position: projectedPosition, data: { label: 'NewEntity', attributes: ['PK id: INT', 'name: VARCHAR(255)'] }, style: { width: 180, height: 120, backgroundColor: '#f0f9ff', border: '1px solid #90cdf4' } };
      break;
    case 'relationshipNode': // ERD relationship
      newNode = { id: `node-${newNodeId.value++}`, type: nodeType, position: projectedPosition, data: { label: '관계' }, style: { width: 100, height: 60, backgroundColor: '#fffbe0', border: '1px solid #fbd38d', borderRadius: '50%' } };
      break;
    case 'attributeNode': // ERD attribute
      newNode = { id: `node-${newNodeId.value++}`, type: nodeType, position: projectedPosition, data: { label: '속성' }, style: { width: 120, height: 50, backgroundColor: '#e6fffa', border: '1px solid #81e6d9', borderRadius: '25px' } };
      break;
    case 'pageNode': // Info Structure page
      newNode = { id: `node-${newNodeId.value++}`, type: nodeType, position: projectedPosition, data: { label: '새 페이지' ,items: [], headerColor: '#718096'}, style: { width: 180, height: 100, backgroundColor: '#f0f4f8', border: '1px solid #cbd5e1' } };
      break;
    case 'sequence-lifeline':
      newNode = { id: `node-${newNodeId.value++}`, type: 'classNode', position: projectedPosition, data: { label: '객체' }, style: { width: 120, height: 400 } };
      break;
    case 'sequence-actor':
      newNode = { id: `node-${newNodeId.value++}`, type: 'usecase-actor', position: projectedPosition, data: { label: '액터' } };
      break;
    case 'sequence-loop':
      newNode = { id: `node-${newNodeId.value++}`, type: 'classNode', position: projectedPosition, data: { label: 'loop' }, style: { width: 300, height: 200, border: '1px dashed #999' } };
      break;
    case 'sequence-alt':
      newNode = { id: `node-${newNodeId.value++}`, type: 'classNode', position: projectedPosition, data: { label: 'alt' }, style: { width: 300, height: 200, border: '1px dashed #999' } };
      break;
    case 'sequence-opt':
      newNode = { id: `node-${newNodeId.value++}`, type: 'classNode', position: projectedPosition, data: { label: 'opt' }, style: { width: 300, height: 200, border: '1px dashed #999' } };
      break;
    default:
      console.warn('알 수 없는 노드 타입이 드롭되었습니다:', nodeType);
      return;
  }

  if (newNode) {
    allDiagramData.value[activeTab.value].nodes.push(newNode);
  }
}

function onNodeDragStop() {}

// === 저장 관련 ===
const saveStatus = ref('idle')
const saveDiagramData = debounce(async () => {
  const readonly = route.query.readonly === 'true'
  if (readonly) {
    console.log('🔒 읽기 전용 모드입니다. 저장하지 않습니다.')
    return
  }
  
  // 유스케이스 탭이 아닐 경우 저장을 건너뜁니다.
  if (activeTab.value !== 'usecase') {
    console.log(`ℹ️ ${activeTab.value} 다이어그램은 현재 저장되지 않습니다. (테스트 목적)`);
    return;
  }

  saveStatus.value = 'saving'

  // 현재 활성화된 탭의 데이터를 가져옵니다.
  const currentDiagramData = allDiagramData.value[activeTab.value];
  if (!currentDiagramData) {
    console.error(`현재 활성화된 탭 (${activeTab.value})의 데이터를 찾을 수 없습니다.`);
    saveStatus.value = 'error';
    return;
  }

  const formData = new FormData()
  formData.append('type', activeTab.value) // 현재 활성화된 탭의 ID를 type으로 전송
  formData.append('json', JSON.stringify(currentDiagramData)) // 현재 탭의 데이터 전송
  formData.append('projectId', route.params.projectId);

  try {
    await api.post('/design/upload', formData);
    saveStatus.value = 'saved'
    setTimeout(() => saveStatus.value = 'idle', 1200)
    console.log(`✅ ${activeTab.value} 다이어그램 저장 성공`)
  } catch (err) {
    console.error(`❌ ${activeTab.value} 다이어그램 저장 실패:`, err)
    saveStatus.value = 'error'
    setTimeout(() => saveStatus.value = 'idle', 3000)
    alert(`⚠️ ${activeTab.value} 저장 중 오류 발생`)
  }
}, 1000)

watch([activeNodes, activeEdges], saveDiagramData, { deep: true })

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

      // === [수정됨] ===
      // DB에서 불러온 모든 노드를 확인하여 가장 큰 ID를 찾습니다.
      let maxId = 0;
      
      // 현재는 유스케이스만 불러오지만, 모든 다이어그램을 순회하도록 처리
      Object.values(allDiagramData.value).forEach(diagram => {
        diagram.nodes.forEach(node => {
          // ID 형식이 'node-10', 'node-11' 등으로 가정
          const idParts = node.id.split('-');
          if (idParts.length === 2) {
            const idNum = parseInt(idParts[1], 10);
            if (!isNaN(idNum) && idNum > maxId) {
              maxId = idNum;
            }
          }
        });
      });

      // 찾은 가장 큰 ID + 1로 새 노드 ID 카운터를 설정합니다.
      // (기존 노드가 없으면 maxId는 0이므로, 10부터 시작하도록 보정)
      newNodeId.value = Math.max(10, maxId + 1);
      console.log(`[중복 방지] 새 노드 ID 시작값을 ${newNodeId.value}로 설정합니다.`);
      // ===============

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

<style>

@import '@vue-flow/node-resizer/dist/style.css';

/* Vue Flow 연결선을 진한 검정색으로 변경 */
.vue-flow__edge-path {
  stroke: #000000 !important;
  stroke-width: 2 !important;
}

/* 화살촉 색상도 검정으로 통일 */
.vue-flow__arrowhead path,
.custom-arrowhead-path { /* [수정] 이 부분을 추가하세요. */
  fill: #000000 !important;
  stroke: #000000 !important;
}

.color-swatch {
  width: 36px;  /* 28px -> 36px로 변경 (원하는 크기로 조절) */
  height: 36px; /* 28px -> 36px로 변경 */
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  cursor: pointer;
  transition: transform 0.1s ease;
}

</style>
