<template>
  <div class="diagram-canvas-container" @dragover="onDragOver" @drop="onDrop">
    <VueFlow
      ref="vueFlowRef"
      :nodes="props.nodes"
      :edges="props.edges"
      :node-types="nodeTypes"
      :edge-types="edgeTypes"
      edges-up-front
      @connect="onConnect"
      @nodes-drag-stop="onNodeDragStop"
      @move-end="onViewportChange"
      
      @update:nodes="emit('update:nodes', $event)"
      @update:edges="emit('update:edges', $event)"

      @node-context-menu="$emit('node-context-menu', $event)"
      @edge-context-menu="$emit('edge-context-menu', $event)"
      @move-start="$emit('move-start', $event)"
    >
      <Background />
      <Controls />
      <!-- [수정] Vue Flow v1.x 방식에 맞게 마커를 슬롯으로 정의 -->
      <template #marker-arrow-closed>
        <marker
          id="arrow-closed"
          viewBox="0 0 10 10" 
          refX="10" refY="5"
          markerWidth="8"
          markerHeight="8"
          orient="auto"
        >
          <path d="M 0 0 L 10 5 L 0 10 z" fill="#000000" />
        </marker>
      </template>
    </VueFlow>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, markRaw, computed } from 'vue'
import { VueFlow, useVueFlow } from '@vue-flow/core'
import { Background } from '@vue-flow/background'
import { Controls } from '@vue-flow/controls'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'

// 정보구조도 전용 노드
import CustomPageNode from './CustomPageNode.vue'
// (VueFlowEditor에서 임시로 사용하던 CustomClassNode 관련 import가 있다면 제거)

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

// 정보구조도 전용 nodeTypes
const nodeTypes = {
  pageNode: markRaw(CustomPageNode),
  // (임시로 CustomClassNode를 사용했다면 아래처럼 수정)
  // entityNode: markRaw(CustomClassNode), 
  // relationshipNode: markRaw(CustomClassNode),
  // attributeNode: markRaw(CustomClassNode),
};
const edgeTypes = {};

function onConnect(connectionParams) {
  const newEdge = {
    id: `e${connectionParams.source}${connectionParams.sourceHandle}-${connectionParams.target}${connectionParams.targetHandle}-${Date.now()}`,
    source: connectionParams.source,
    target: connectionParams.target,
    type: 'default',
    sourceHandle: connectionParams.sourceHandle,
    targetHandle: connectionParams.targetHandle,
    data: { label: '', lineStyle: 'none' },
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
  //
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
  if (!nodeType || !['pageNode'].includes(nodeType)) return;

  const containerBounds = event.currentTarget.getBoundingClientRect();
  const position = project({ 
    x: event.clientX - containerBounds.left, 
    y: event.clientY - containerBounds.top 
  });
  const newNodeId = `node-${lastNodeId.value + 1}`;
  let newNode;

  switch(nodeType) {
    case 'pageNode':
      newNode = { id: newNodeId, type: 'pageNode', position, data: { label: '새 페이지' ,items: [], headerColor: '#718096'}, style: { width: 180, height: 100, backgroundColor: '#f0f4f8', border: '1px solid #cbd5e1' } };
      break;
  }

  if (newNode) {
    const newNodes = [...props.nodes, newNode];
    emit('update:nodes', newNodes);
  }
}

// --- 뷰포트 로직 ---
const activeTabId = 'infostructure'; // [수정] 탭 ID

function onViewportChange() {
  if (vueFlowRef.value) {
    const viewport = vueFlowRef.value.toObject().viewport;
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

<style scoped>
.diagram-canvas-container {
  width: 100%;
  height: 100%;
  position: relative;
}
</style>