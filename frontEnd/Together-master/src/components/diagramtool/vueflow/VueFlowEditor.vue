<template>
  <div class="editor-layout">
    <DiagramTabs 
      :active-tab-id="activeTab"
      @tab-changed="onTabChange" 
    />

    <div 
      class="canvas-wrapper" 
      ref="flowWrapper" 
      @click="hideContextMenu" 
    > 
      <!-- 클래스 다이어그램일 때는 전체 페이지를 렌더링 -->
      <ClassDiagramPage v-if="activeTab === 'classDiagram'" :key="`class-${activeTab}`" />
      
      <!-- ERD일 때는 전체 페이지를 렌더링 -->
      <ErdDiagramPage v-else-if="activeTab === 'erd'" :key="`erd-${activeTab}`" />
      
      <!-- 시퀀스 다이어그램일 때는 전체 페이지를 렌더링 -->
      <SequenceDiagramPage v-else-if="activeTab === 'sequence'" :key="`sequence-${activeTab}`" />
      
      <!-- 다른 다이어그램들은 기존 방식 유지 -->
      <template v-else>
        <component
          v-if="currentDiagramComponent"
          :is="currentDiagramComponent"
          :nodes="activeNodes"
          :edges="activeEdges"
          @update:nodes="allDiagramData[activeTab].nodes = $event"
          @update:edges="allDiagramData[activeTab].edges = $event"

          @node-context-menu="onNodeContextMenu"
          @edge-context-menu="onEdgeContextMenu"
          :key="activeTab"
          @move-start="hideContextMenu"
        />
        
        <Toolbox :active-tab="activeTab" />
      </template>

      <div
        v-if="contextMenu.visible" 
        class="context-menu" 
        :style="{ top: `${contextMenu.y}px`, left: `${contextMenu.x}px` }"
        @click.stop
      >
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
              <option value="default">직선</option>
              <option value="step">직각 선</option>
              <option value="smoothstep">곡선</option>
            </select>
          </div>

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

          <template v-else-if="activeTab === 'classDiagram'">
            <div class="menu-section">
              <div class="menu-label">시작 모양</div>
              <select class="menu-select" @change="setEdgeMarkerStart($event.target.value)" :value="getMarkerId(currentContextMenuTargetEdge.markerStart)">
                <option value="">없음</option>
                <option value="diamond-aggregation">집합 (빈 마름모)</option>
                <option value="diamond-composition">복합 (채워진 마름모)</option>
              </select>
            </div>
            <div class="menu-section">
              <div class="menu-label">끝 모양</div>
              <select class="menu-select" @change="setEdgeMarkerEnd($event.target.value)" :value="getMarkerId(currentContextMenuTargetEdge.markerEnd)">
                <option value="">없음</option>
                <option value="arrow-generalization">일반화 (빈 삼각형)</option>
                <option value="arrow-dependency">의존 (화살표)</option>
                <option value="diamond-aggregation">집합 (빈 마름모)</option>
                <option value="diamond-composition">복합 (채워진 마름모)</option>
              </select>
            </div>
            <div class="menu-section">
              <div class="menu-label">라벨</div>
              <button class="menu-item" @click="setEdgeLabel('1..*')">1..*</button>
              <button class="menu-item" @click="setEdgeLabel('0..1')">0..1</button>
              <button class="menu-item" @click="setEdgeLabel('')">라벨 삭제</button>
            </div>
          </template>

          <template v-else-if="activeTab === 'erd'">
          </template>

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
          <div class="menu-section">
            <div class="menu-label">노드 옵션</div>
          </div>
        </div>
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
import { ref, computed, ref as vueRef, markRaw, watch, onMounted, nextTick } from 'vue'
import { useRoute } from 'vue-router'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

// [추가] 캔버스 컴포넌트 import
import ClassDiagramPage from '@/views/toolview/ClassDiagramPage.vue'
import ErdDiagramPage from '@/views/toolview/ErdDiagramPage.vue'
import SequenceDiagramPage from '@/views/toolview/SequenceDiagramPage.vue'
import UsecaseDiagramCanvas from './Usecase/UsecaseDiagramCanvas.vue'
import InfoStructureDiagramCanvas from './InfoStructure/InfoStructureDiagramCanvas.vue'
// (InfoStructureDiagramCanvas.vue 파일도 Usecase처럼 만들어야 합니다)
import Toolbox from '@/components/diagramtool/vueflow/Toolbox.vue' 
import DiagramTabs from '@/components/diagramtool/vueflow/DiagramTabs.vue' 

const emit = defineEmits(['update:nodes', 'update:edges']);

const props = defineProps({
  projectId: [String, Number],
  readonly: Boolean,
  initialTab: { type: String, default: 'classDiagram' }
});

import { MarkerType } from '@vue-flow/core'
// [추가] 캔버스 컴포넌트 매핑 (classDiagram, erd, sequence는 별도로 처리)
const diagramComponents = {
  usecase: markRaw(UsecaseDiagramCanvas),
  infostructure: markRaw(InfoStructureDiagramCanvas),
};

// [추가] 현재 탭에 맞는 캔버스 컴포넌트 선택
const currentDiagramComponent = computed(() => {
  // classDiagram, erd, sequence는 제외 (별도로 전체 페이지 렌더링)
  if (activeTab.value === 'classDiagram' || activeTab.value === 'erd' || activeTab.value === 'sequence') return null;
  return diagramComponents[activeTab.value] || null;
});

const flowWrapper = vueRef(null);
const route = useRoute();
const activeTab = ref(props.initialTab || localStorage.getItem('lastActiveDiagramTab') || 'classDiagram');

const contextMenu = ref({
  visible: false,
  x: 0,
  y: 0,
  target: null,
});

const allDiagramData = ref({
  classDiagram: {nodes: [ ],edges: [ ], },
  usecase: { nodes: [], edges: [] },
  sequence: { nodes: [], edges: [] },
  erd: { nodes: [], edges: [] },
  infostructure: { nodes: [], edges: [] },
});

// (유지) activeNodes, activeEdges
const activeNodes = computed({
  get: () => allDiagramData.value[activeTab.value]?.nodes || [],
  // [삭제] set 로직 제거
});
const activeEdges = computed({
  get: () => allDiagramData.value[activeTab.value]?.edges || [],
  // [삭제] set 로직 제거
});


function onTabChange(tabId) {
  activeTab.value = tabId;
  localStorage.setItem('lastActiveDiagramTab', tabId);
}

// (유지) currentContextMenuTargetNode, currentContextMenuTargetEdge
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

// (유지) getEdgeProperty (컨텍스트 메뉴용)
function getEdgeProperty(key) {
  if (contextMenu.value.target?.type !== 'edge') return null;
  const edge = activeEdges.value.find(e => e.id === contextMenu.value.target.id);
  if (!edge) return null;
  if (key === 'markerEnd') return edge.markerEnd || '';
  if (key === 'markerStart') return edge.markerStart || '';
  
  return edge.data ? edge.data[key] : null;
}

// (유지) setEdgeType (컨텍스트 메뉴용)
function setEdgeType(type) {
  const { id } = contextMenu.value.target;
  if (!id) return; 

  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    const edgeToUpdate = { ...updatedEdges[edgeIndex], type: type };
    updatedEdges[edgeIndex] = edgeToUpdate;
    allDiagramData.value[activeTab.value].edges = updatedEdges; // [수정] 직접 데이터 소스를 변경
  }
}

function getMarkerId(marker) {
  if (typeof marker === 'string' && marker.startsWith('url(#')) {
    return marker.slice(5, -1);
  }
  return marker?.type || '';
}

// (유지) onNodeContextMenu, onEdgeContextMenu (자식에게 이벤트를 받음)
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

// [수정] hideContextMenu
function hideContextMenu() {
  if (contextMenu.value.visible) {
    contextMenu.value.visible = false;
  }
}

// (유지) deleteContextMenuTarget 및 모든 setEdge.../setNode... 함수
function deleteContextMenuTarget() {
  const { type, id } = contextMenu.value.target;
  if (!type || !id) return;

  if (type === 'node') {
    activeNodes.value = activeNodes.value.filter(node => node.id !== id);
    activeEdges.value = activeEdges.value.filter(edge => edge.source !== id && edge.target !== id);
    allDiagramData.value[activeTab.value].nodes = activeNodes.value.filter(node => node.id !== id);
    allDiagramData.value[activeTab.value].edges = activeEdges.value.filter(edge => edge.source !== id && edge.target !== id);
  } else if (type === 'edge') {
    allDiagramData.value[activeTab.value].edges = activeEdges.value.filter(edge => edge.id !== id);
  }

  hideContextMenu();
}

function setEdgeStyle(lineStyle) {
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    const newStyle = {
      ...updatedEdges[edgeIndex].style,
      strokeDasharray: lineStyle === 'dashed' ? '5 5' : undefined,
    };
    updatedEdges[edgeIndex] = { ...updatedEdges[edgeIndex], style: newStyle };
    updatedEdges[edgeIndex].data.lineStyle = lineStyle;
    allDiagramData.value[activeTab.value].edges = updatedEdges; // [수정] 직접 데이터 소스를 변경
  }
}

function setEdgeMarkerStart(markerValue) { 
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    let newMarkerStart;

    if (activeTab.value === 'classDiagram') {
      newMarkerStart = markerValue ? `url(#${markerValue})` : undefined;
      // [수정] 커스텀 마커가 올바르게 표시되도록 엣지 타입을 'default'(직선)로 강제합니다.
      if (markerValue) {
        updatedEdges[edgeIndex].type = 'default';
      }
    } else {
      switch (markerValue) {
        case 'arrowclosed':
          newMarkerStart = { type: MarkerType.ArrowClosed, color: '#000000', width: 15, height: 15 };
          break;
        default:
          newMarkerStart = undefined;
          break;
      }
    }

    const edgeToUpdate = { ...updatedEdges[edgeIndex], markerStart: newMarkerStart };
    updatedEdges[edgeIndex] = edgeToUpdate;
    allDiagramData.value[activeTab.value].edges = updatedEdges;
  }
}

function setEdgeMarkerEnd(markerValue) { 
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    let newMarkerEnd;

    if (activeTab.value === 'classDiagram') {
      newMarkerEnd = markerValue ? `url(#${markerValue})` : undefined;
      // [수정] 어떤 마커든 선택되면 엣지 타입을 'default'(직선)로 강제합니다.
      if (markerValue) {
        updatedEdges[edgeIndex].type = 'default';
      }
    } else {
      switch (markerValue) {
        case 'arrowclosed':
          newMarkerEnd = { type: MarkerType.ArrowClosed, color: '#000000', width: 15, height: 15 };
          break;
        default:
          newMarkerEnd = undefined;
          break;
      }
    }

    const edgeToUpdate = { ...updatedEdges[edgeIndex], markerEnd: newMarkerEnd };
    updatedEdges[edgeIndex] = edgeToUpdate;
    allDiagramData.value[activeTab.value].edges = updatedEdges;
  }
}

function setEdgeLabel(label) {
  const { id } = contextMenu.value.target;
  if (!id) return;
  const edgeIndex = activeEdges.value.findIndex(edge => edge.id === id);
  if (edgeIndex !== -1) {
    const updatedEdges = [...activeEdges.value];
    
    const edgeToUpdate = { ...updatedEdges[edgeIndex] };
    edgeToUpdate.data = { ...edgeToUpdate.data, label: label };
    edgeToUpdate.label = label; 

    updatedEdges[edgeIndex] = edgeToUpdate;
    allDiagramData.value[activeTab.value].edges = updatedEdges; // [수정] 직접 데이터 소스를 변경
  }
  hideContextMenu();
}

function setNodeHeaderColor(color) {
  const { id } = contextMenu.value.target;
  if (!id) return; 

  const node = activeNodes.value.find(n => n.id === id);
  if (node) {
    node.data = { ...node.data, headerColor: color };
  }
  
  hideContextMenu(); 
}

// (유지) 저장 관련 로직
import api from '@/api'
import { debounce } from 'lodash'
const saveStatus = ref('idle')
const saveDiagramData = debounce(async () => {
  const readonly = route.query.readonly === 'true'
  if (readonly) {
    console.log('🔒 읽기 전용 모드입니다. 저장하지 않습니다.')
    return
  }
  
  saveStatus.value = 'saving'
  const currentDiagramData = allDiagramData.value[activeTab.value];
  if (!currentDiagramData) {
    console.error(`현재 활성화된 탭 (${activeTab.value})의 데이터를 찾을 수 없습니다.`);
    saveStatus.value = 'error';
    return;
  }

  // [수정] 정보구조도는 /planning/update, 나머지는 /design/upload로 분기
  if (activeTab.value === 'infostructure') {
    const formData = new FormData();
    formData.append('type', 'infostructure');
    formData.append('projectId', route.params.projectId);
    formData.append('json', JSON.stringify(currentDiagramData));
    // formData.append('text', ''); // 텍스트 입력 UI가 있다면 여기에 추가

    try {
      await api.put('/planning/update', formData);
      saveStatus.value = 'saved';
      setTimeout(() => saveStatus.value = 'idle', 1200);
      console.log(`✅ 정보구조도 저장 성공`);
    } catch (err) {
      console.error(`❌ 정보구조도 저장 실패:`, err);
      saveStatus.value = 'error';
      setTimeout(() => saveStatus.value = 'idle', 3000);
      alert(`⚠️ 정보구조도 저장 중 오류 발생`);
    }
  } else {
    const formData = new FormData();
    formData.append('type', activeTab.value);
    formData.append('json', JSON.stringify(currentDiagramData));
    formData.append('projectId', route.params.projectId);

    try {
      await api.post('/design/upload', formData);
      saveStatus.value = 'saved';
      setTimeout(() => saveStatus.value = 'idle', 1200);
      console.log(`✅ ${activeTab.value} 다이어그램 저장 성공`);
    } catch (err) {
      console.error(`❌ ${activeTab.value} 다이어그램 저장 실패:`, err);
      saveStatus.value = 'error';
      setTimeout(() => saveStatus.value = 'idle', 3000);
      alert(`⚠️ ${activeTab.value} 저장 중 오류 발생`);
    }
  }
}, 1000)

watch(allDiagramData, saveDiagramData, { deep: true })

// (유지) onMounted (데이터 로딩 및 newNodeId 설정)

onMounted(async () => {
  try {
    const res = await api.get('/design/all', {
      params: { projectId: route.params.projectId }
    })
    
    // 1. 설계 다이어그램 데이터 로드
    const { usecase, classDiagram, erd, sequence } = res.data 
    if (usecase?.json) {
      const parsed = JSON.parse(usecase.json)
      allDiagramData.value.usecase = { nodes: parsed.nodes || [], edges: parsed.edges || [] };
      console.log('✅ 유스케이스 불러오기 성공:', parsed)
    }
    if (classDiagram?.json) {
      const parsed = JSON.parse(classDiagram.json)
      allDiagramData.value.classDiagram = { nodes: parsed.nodes || [], edges: parsed.edges || [] };
      console.log('✅ 클래스 다이어그램 불러오기 성공:', parsed)
    }
    if (erd?.json) {
      const parsed = JSON.parse(erd.json)
      allDiagramData.value.erd = { nodes: parsed.nodes || [], edges: parsed.edges || [] };
      console.log('✅ ERD 불러오기 성공:', parsed)
    }
    if (sequence?.json) {
      const parsed = JSON.parse(sequence.json)
      allDiagramData.value.sequence = { nodes: parsed.nodes || [], edges: parsed.edges || [] };
      console.log('✅ 시퀀스 다이어그램 불러오기 성공:', parsed)
    }

    // 2. 기획 다이어그램(정보구조도) 데이터 별도 로드
    const planningRes = await api.get('/planning/all', {
      params: { projectId: route.params.projectId }
    });
    if (planningRes.data?.infostructure?.json) {
      const parsed = JSON.parse(planningRes.data.infostructure.json);
      allDiagramData.value.infostructure = { nodes: parsed.nodes || [], edges: parsed.edges || [] };
      console.log('✅ 정보구조도 불러오기 성공:', parsed);
    }

    // (유지) newNodeId 설정 로직
    let maxId = 0;
    Object.values(allDiagramData.value).forEach(diagram => {
      if (diagram.nodes) { // 노드가 null/undefined가 아닌지 확인
        diagram.nodes.forEach(node => {
          const idParts = String(node.id).split('-'); // id가 숫자인 경우도 대비
          if (idParts.length === 2) {
            const idNum = parseInt(idParts[1], 10);
            if (!isNaN(idNum) && idNum > maxId) {
              maxId = idNum;
            }
          } else if (typeof node.id === 'number') { // 숫자 ID 처리
            if (node.id > maxId) maxId = node.id;
          }
        });
      }
    });
    console.log(`[데이터 로드 완료] 모든 다이어그램의 데이터를 불러왔습니다.`);
  } catch (err) {
    console.error('❌ 초기 데이터 로드 실패:', err)
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

/* ClassDiagramPage, ErdDiagramPage, SequenceDiagramPage만 전체 영역을 차지하도록 */
.canvas-wrapper .diagram-layout,
.canvas-wrapper .erd-layout {
  width: 100%;
  height: 100%;
  position: relative;
  z-index: 1;
}
</style>
<style>
/* (유지) context-menu, save-toast, .vue-flow__edge-path, .color-swatch 스타일 */
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
  z-index: 9999;
  transition: opacity 0.3s;
  pointer-events: none;
  white-space: nowrap;
  display: inline-block;
  width: auto;
  height: auto;
}
.save-toast.saving { background-color: #777; }
.save-toast.saved { background-color: #323232; }
.save-toast.error { background-color: #dc3545; }

@import '@vue-flow/node-resizer/dist/style.css';

.vue-flow__edge-path {
  stroke: #000000 !important;
  stroke-width: 2 !important;
}
.vue-flow__arrowhead path,
.custom-arrowhead-path {
  stroke: #000000 !important;
}
.color-swatch {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.2);
  cursor: pointer;
  transition: transform 0.1s ease;
}
</style>
