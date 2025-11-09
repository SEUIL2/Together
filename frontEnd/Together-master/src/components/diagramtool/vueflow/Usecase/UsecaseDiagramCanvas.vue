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

// 유스케이스 전용 노드
import CustomActorNode from './CustomActorNode.vue'
import CustomUsecaseNode from './CustomUsecaseNode.vue'
import CustomSystemNode from './CustomSystemNode.vue'
import CustomNoteNode from './CustomNoteNode.vue'

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

// 유스케이스 전용 nodeTypes
const nodeTypes = {
  'usecase-actor': markRaw(CustomActorNode),
  'usecase-bubble': markRaw(CustomUsecaseNode),
  'usecase-system': CustomSystemNode,
  note: markRaw(CustomNoteNode),
};
const edgeTypes = {
  // 'usecase-edge': markRaw(CustomUsecaseEdge) // 커스텀 엣지가 있다면
};

function onConnect(connectionParams) {
  // 유스케이스 전용 onConnect 로직
  const newEdge = {
    id: `e${connectionParams.source}${connectionParams.sourceHandle}-${connectionParams.target}${connectionParams.targetHandle}-${Date.now()}`,
    source: connectionParams.source,
    target: connectionParams.target,
    type: 'default', // 'usecase-edge' 등
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
  if (!nodeType || !['usecase-actor', 'usecase-bubble', 'usecase-system', 'note'].includes(nodeType)) return;

  const position = project({ x: event.clientX, y: event.clientY });
  const newNodeId = `node-${lastNodeId.value + 1}`;
  let newNode;

  switch(nodeType) {
    case 'usecase-actor':
      newNode = { id: newNodeId, type: 'usecase-actor', position, data: { label: '액터' } };
      break;
    case 'usecase-bubble':
      newNode = { id: newNodeId, type: 'usecase-bubble', position, data: { label: '유스케이스' } };
      break;
    case 'usecase-system':
      newNode = { id: newNodeId, type: 'usecase-system', position, data: { label: '시스템 경계' }, style: { width: '400px', height: '300px' }};
      break;
    case 'note':
      newNode = { id: newNodeId, type: 'note', position, data: { label: '노트...' }, style: { width: 150, height: 80, backgroundColor: '#fffacd', border: '1px solid #e0e0e0' }};
      break;
  }

  if (newNode) {
    const newNodes = [...props.nodes, newNode];
    emit('update:nodes', newNodes);
  }
}


// --- 뷰포트 로직 ---
const activeTabId = 'usecase';

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