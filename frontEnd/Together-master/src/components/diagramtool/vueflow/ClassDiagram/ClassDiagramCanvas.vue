<template>
  <div class="diagram-canvas-container" @dragover="onDragOver" @drop="onDrop" @click="handleClickOutside">
    <VueFlow
      ref="vueFlowRef"
      :nodes="props.nodes"
      :edges="props.edges"
      :node-types="nodeTypes"
      :edge-types="edgeTypes"
      :default-viewport="defaultViewport"
      edges-up-front
      :zoom-on-double-click="false"
      @connect="onConnect"
      @nodes-drag-stop="onNodeDragStop"
      @move-end="onViewportChange"
      @node-double-click="handleNodeDoubleClick"
      @node-click="handleNodeClick"
      @update:nodes="emit('update:nodes', $event)"
      @update:edges="emit('update:edges', $event)"
      @node-context-menu="$emit('node-context-menu', $event)"
      @edge-context-menu="$emit('edge-context-menu', $event)"
      @move-start="$emit('move-start', $event)"
    >
      <Background />
      <Controls />

      <!-- 
        [최종 수정] Vue Flow 공식 예제를 기반으로 한, 가장 표준적인 마커 정의입니다.
        이 코드는 다른 설정과 무관하게 독립적으로 작동해야 합니다.
      -->
      <svg>
        <defs>
          <!-- 일반화 (빈 삼각형) -->
          <marker
            id="arrow-generalization"
            viewBox="-10 -5 10 10"
            refX="-1"
            refY="0"
            markerUnits="strokeWidth"
            markerWidth="10"
            markerHeight="10"
            orient="auto"
          >
            <path d="M -10 -5 L 0 0 L -10 5 z" fill="#fff" stroke="#000" stroke-width="1.5" />
          </marker>

          <!-- 의존 (화살표) -->
          <marker
            id="arrow-dependency"
            viewBox="-10 -5 10 10"
            refX="-1"
            refY="0"
            markerUnits="strokeWidth"
            markerWidth="10"
            markerHeight="10"
            orient="auto"
          >
            <path d="M 0 -5 L -10 0 L 0 5" fill="none" stroke="#000" stroke-width="1.5" />
          </marker>

          <!-- 집합 (빈 마름모) -->
          <marker
            id="diamond-aggregation"
            viewBox="-20 -10 20 20"
            refX="-1"
            refY="0"
            markerUnits="strokeWidth"
            markerWidth="12"
            markerHeight="12"
            orient="auto"
          >
            <path d="M 0 0 L -10 -7 L -20 0 L -10 7 z" fill="#fff" stroke="#000" stroke-width="1.5" />
          </marker>

          <!-- 복합 (채워진 마름모) -->
          <marker
            id="diamond-composition"
            viewBox="-20 -10 20 20"
            refX="-1"
            refY="0"
            markerUnits="strokeWidth"
            markerWidth="12"
            markerHeight="12"
            orient="auto"
          >
            <path d="M 0 0 L -10 -7 L -20 0 L -10 7 z" fill="#000" stroke="#000" stroke-width="1.5" />
          </marker>
        </defs>
      </svg>
    </VueFlow>

    <!-- 노드 이름 수정용 오버레이 (기존 코드 유지) -->
    <div
      v-if="editing.visible"
      class="overlay-editbox"
      :style="{ top: editing.y + 'px', left: editing.x + 'px' }"
      @click.stop
    >
      <input
        class="editbox-input"
        v-model="editing.value"
        @keydown.enter="applyEdit"
        @blur="applyEdit"
        autofocus
        placeholder="내용 입력"
      />
      <button
        v-if="editing.type !== 'name'"
        class="editbox-delbtn"
        @mousedown.prevent="deleteEditingItem"
        title="삭제"
      >
        <svg width="20" height="20" viewBox="0 0 20 20">
          <g>
            <rect x="6" y="8.7" width="2" height="6" rx="1"/>
            <rect x="12" y="8.7" width="2" height="6" rx="1"/>
            <path d="M4.3 6.3h11.4l-1.1 10.1a1 1 0 0 1-1 .9H6.4a1 1 0 0 1-1-.9L4.3 6.3zm3.2-2.1a1 1 0 0 1 1-1h3a1 1 0 0 1 1 1v1.1h-5V4.2z" />
          </g>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
// 스크립트 부분은 기존 코드를 그대로 유지합니다.
import { ref, reactive, onMounted, nextTick, markRaw, computed } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

import CustomClassNode from './CustomClassNode.vue' 
import CustomNoteNode from '../Usecase/CustomNoteNode.vue'

const props = defineProps({
  nodes: { type: Array, required: true },
  edges: { type: Array, required: true }
})
const emit = defineEmits([
  'update:nodes', 
  'update:edges',
  'node-context-menu',
  'edge-context-menu',
  'move-start'
])

const vueFlowRef = ref(null)
const { project } = useVueFlow()

const nodeTypes = {
  classNode: markRaw(CustomClassNode),
  interfaceNode: markRaw(CustomClassNode),
  enumNode: markRaw(CustomClassNode),
  note: markRaw(CustomNoteNode),
};
const edgeTypes = {
};

const editing = reactive({
  visible: false,
  nodeId: null,
  type: '',
  index: null,
  x: 0, y: 0,
  value: ''
});

function openEditor(node, type, index, value, originalEvent) {
  if (!vueFlowRef.value) return;
  const containerBounds = vueFlowRef.value.wrapperRef.getBoundingClientRect();

  editing.visible = true;
  editing.nodeId = node.id;
  editing.type = type;
  editing.index = index;
  editing.value = value;
  
  editing.x = originalEvent.clientX - containerBounds.left;
  editing.y = originalEvent.clientY - containerBounds.top;

  emit('move-start'); 

  nextTick(() => {
    const input = document.querySelector('.editbox-input');
    if (input) {
      input.focus();
      input.select();
    }
  });
}

function applyEdit() {
  if (!editing.visible) return;
  
  const nodeIndex = props.nodes.findIndex(n => n.id === editing.nodeId);
  if (nodeIndex === -1) {
    editing.visible = false;
    return;
  }
  
  const node = props.nodes[nodeIndex];
  const newData = { ...node.data }; 

  if (editing.type === 'name') {
    newData.name = editing.value;
  } else if (editing.type === 'attribute') {
    const newAttributes = [...(newData.attributes || [])]; 
    newAttributes[editing.index] = editing.value;
    newData.attributes = newAttributes;
  } else if (editing.type === 'method') {
    const newMethods = [...(newData.methods || [])];
    newMethods[editing.index] = editing.value;
    newData.methods = newMethods;
  }
  
  const newNodes = [...props.nodes];
  newNodes[nodeIndex] = { ...node, data: newData };
  emit('update:nodes', newNodes);
  
  editing.visible = false;
}

function deleteEditingItem() {
  if (!editing.visible || editing.type === 'name') return;
  
  const nodeIndex = props.nodes.findIndex(n => n.id === editing.nodeId);
  if (nodeIndex === -1) return;

  const node = props.nodes[nodeIndex];
  const newData = { ...node.data };

  if (editing.type === 'attribute') {
    const newAttributes = [...(newData.attributes || [])];
    newAttributes.splice(editing.index, 1);
    newData.attributes = newAttributes;
  } else if (editing.type === 'method') {
    const newMethods = [...(newData.methods || [])];
    newMethods.splice(editing.index, 1);
    newData.methods = newMethods;
  }

  const newNodes = [...props.nodes];
  newNodes[nodeIndex] = { ...node, data: newData };
  emit('update:nodes', newNodes);
  
  editing.visible = false;
}

function handleNodeDoubleClick(nodeMouseEvent) {
  const target = nodeMouseEvent.event.target;
  const type = target.dataset.type;
  if (!type) return;

  const node = nodeMouseEvent.node;

  if (type === 'name' || type === 'attribute' || type === 'method') {
    const index = target.dataset.index ? parseInt(target.dataset.index, 10) : null;
    let value = '';

    if (type === 'name') {
      value = node.data.name;
    } else if (type === 'attribute' && node.data.attributes) {
      value = node.data.attributes[index];
    } else if (type === 'method' && node.data.methods) {
      value = node.data.methods[index];
    }
    
    openEditor(node, type, index, value, nodeMouseEvent.event);
  }
}

function handleNodeClick(nodeMouseEvent) {
  const target = nodeMouseEvent.event.target;
  const type = target.dataset.type;
  if (!type) return;

  const node = nodeMouseEvent.node;
  const nodeIndex = props.nodes.findIndex(n => n.id === node.id);
  if (nodeIndex === -1) return;

  const newData = { ...node.data };
  let needsUpdate = false;

  if (type === 'add-attribute') {
    newData.attributes = [...(newData.attributes || []), '+ newAttribute: Type'];
    needsUpdate = true;
  } else if (type === 'add-method') {
    newData.methods = [...(newData.methods || []), '+ newMethod(): void'];
    needsUpdate = true;
  }

  if (needsUpdate) {
    const newNodes = [...props.nodes];
    newNodes[nodeIndex] = { ...node, data: newData };
    emit('update:nodes', newNodes);
  }

  if (editing.visible) {
    editing.visible = false;
  }
}

function handleClickOutside() {
  if (editing.visible) {
    applyEdit();
  }
}

const lastNodeId = computed(() => {
  return Math.max(0, ...props.nodes.map(n => {
    const idNum = parseInt(String(n.id).split('-').pop(), 10);
    return isNaN(idNum) ? 0 : idNum;
  }));
});

function onDragOver(event) {
  event.preventDefault();
  if (event.dataTransfer) {
    event.dataTransfer.dropEffect = 'copy';
  }
}

function onDrop(event) {
  event.preventDefault();
  const nodeType = event.dataTransfer?.getData('application/node');
  if (!nodeType || !['classNode', 'interfaceNode', 'enumNode', 'note'].includes(nodeType)) return;
  
  const containerBounds = event.currentTarget.getBoundingClientRect();
  const position = project({ 
    x: event.clientX - containerBounds.left, 
    y: event.clientY - containerBounds.top 
  });
  const newNodeId = `node-${lastNodeId.value + 1}`;
  let newNode;

  switch(nodeType) {
    case 'classNode':
      newNode = {
        id: newNodeId, type: 'classNode', position,
        data: { name: 'NewClass', stereotype: '', attributes: ['+ attribute: Type'], methods: ['+ method(): ReturnType'] },
      };
      break;
    case 'interfaceNode':
      newNode = {
        id: newNodeId, type: 'interfaceNode', position,
        data: { name: 'NewInterface', stereotype: 'interface', attributes: [], methods: ['+ operation()'] },
      };
      break;
    case 'enumNode':
      newNode = {
        id: newNodeId, type: 'enumNode', position,
        data: { name: 'NewEnum', stereotype: 'enum', attributes: ['VALUE1', 'VALUE2'], methods: [] },
      };
      break;
    case 'note':
      newNode = {
        id: newNodeId, type: 'note', position,
        data: { label: '노트...' },
        style: { width: '150px', height: '80px', backgroundColor: '#fffacd', border: '1px solid #e0e0e0' },
      };
      break;
    default:
      return;
  }

  if (newNode) {
    const newNodes = [...props.nodes, newNode];
    emit('update:nodes', newNodes);
  }
}

const defaultViewport = {
  x: 0,
  y: 0,
  zoom: 1,
};

function onConnect(connectionParams) {
  const newEdge = {
    id: `e${connectionParams.source}${connectionParams.sourceHandle}-${connectionParams.target}${connectionParams.targetHandle}-${Date.now()}`,
    source: connectionParams.source,
    target: connectionParams.target,
    type: 'default',
    sourceHandle: connectionParams.sourceHandle,
    targetHandle: connectionParams.targetHandle,

    markerStart: '',
    markerEnd: '',
    data: { 
      label: '',
      lineStyle: 'none',
      relationshipType: 'association'
    },
    label: '', 
    labelStyle: { fill: '#2d3748', fontWeight: 500 }, 
    labelBgStyle: { fill: '#f8f9fa' }, 
    labelBgPadding: [4, 8],
    labelBgBorderRadius: 4, 
    labelYOffset: -20, 
  }
  emit('update:edges', [...props.edges, newEdge]);
}
function onNodeDragStop() {
}

const activeTabId = 'classDiagram'; 

function onViewportChange() {
  if (vueFlowRef.value) {
    const viewport = vueFlowRef.value.getViewport();
    localStorage.setItem(`viewport-${activeTabId}`, JSON.stringify(viewport));
  }
}

function loadViewport() {
  const savedViewportJson = localStorage.getItem(`viewport-${activeTabId}`); 
  nextTick(() => {
    if (savedViewportJson && vueFlowRef.value) {
      try {
        const savedViewport = JSON.parse(savedViewportJson);
        vueFlowRef.value.setViewport(savedViewport, { duration: 0 }); 
      } catch (e) {
        console.error("뷰포트 로드 실패:", e);
        vueFlowRef.value.fitView(); 
      }
    } else if (vueFlowRef.value) {
      vueFlowRef.value.fitView();
    }
  });
}

onMounted(() => {
  loadViewport();
});

</script>

<style>
.diagram-canvas-container {
  width: 100%;
  height: 100%;
  position: relative;
}

.overlay-editbox {
  position: absolute;
  z-index: 1001;
  display: flex;
  align-items: center;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 6px 20px rgba(44,62,80,0.12), 0 1.5px 6px rgba(44,62,80,0.06);
  padding: 8px 14px 8px 14px;
  gap: 10px;
  border: 1.2px solid #d3dae6;
  min-width: 150px;
  min-height: 38px;
  transition: box-shadow 0.18s, border 0.15s;
}
.overlay-editbox:focus-within {
  box-shadow: 0 8px 24px rgba(44,62,80,0.18), 0 2px 10px rgba(44,62,80,0.11);
  border-color: #4ba7fa;
}
.editbox-input {
  border: none;
  outline: none;
  font-size: 16px;
  background: transparent;
  min-width: 110px;
  padding: 2px 3px;
  color: #232b39;
}
.editbox-input::placeholder {
  color: #bbb;
}
.editbox-delbtn {
  background: none;
  border: none;
  padding: 0 3px;
  cursor: pointer;
  transition: filter 0.14s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.editbox-delbtn svg {
  fill: #ec7e7e;
  transition: fill 0.2s;
}
.editbox-delbtn:hover svg {
  fill: #d32f2f;
  filter: drop-shadow(0 0 2px #f58b8b);
}
</style>
