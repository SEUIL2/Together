<template>
  <!-- g 태그로 선과 텍스트를 그룹화하여 함께 이벤트를 처리합니다. -->
  <!-- [수정] 컴포넌트의 최상위 요소에 contextmenu 이벤트를 직접 연결합니다. -->
  <g @contextmenu.prevent>
    <!-- 1. 기본 선 (BaseEdge) -->
    <!-- [수정] <script setup>에서는 props를 직접 사용할 수 있으므로 'props.' 접두사를 제거합니다. -->
    <BaseEdge :id="id" :style="edgeStyle" :path="path[0]" :marker-start="markerStart" :marker-end="markerEnd" />

    <!-- 2. 드래그 가능한 관계선 라벨 -->
    <foreignObject
      :width="labelWidth"
      :height="labelHeight"
      :style="labelStyle"
      style="overflow: visible; text-align: center;"
      @mousedown="onDragStart"
    >
      <div class="edge-label-bg" v-if="data.label" ref="labelRef">
        {{ data.label }}
      </div>
    </foreignObject>

    <!-- [추가] 선 끝지점에 화살표를 직접 그립니다. (디버깅용) -->
    <path
      class="vue-flow__edge-arrow"
      d="M -15 -8 L 0 0 L -15 8 z"
      :style="{
        transform: `translate(${props.sourceX}px, ${props.sourceY}px) rotate(${arrowAngle}deg)`
      }"
    />
  </g>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue'
// [수정] EdgeLabel을 import하지 않습니다.
import { BaseEdge, getBezierPath, useVueFlow } from '@vue-flow/core'

const props = defineProps({
  id: {
    type: String,
    required: true,
  },
  sourceX: {
    type: Number,
    required: true,
  },
  sourceY: {
    type: Number,
    required: true,
  },
  targetX: {
    type: Number,
    required: true,
  },
  targetY: {
    type: Number,
    required: true,
  },
  sourcePosition: {
    type: String,
    required: true,
  },
  targetPosition: {
    type: String,
    required: true,
  },
  data: {
    type: Object,
    default: () => ({}),
  },
  markerEnd: {
    type: String,
    default: '',
  },
  markerStart: { // [추가] 시작 화살표 prop
    type: String,
    default: '',
  },
})

const emit = defineEmits(['update:data'])

const { project } = useVueFlow()

// 라벨 크기 동적 계산을 위한 참조
const labelRef = ref(null);
const labelWidth = ref(100);
const labelHeight = ref(24);

// [추가] 라벨 텍스트 변경 시 라벨 크기를 동적으로 업데이트
watch(() => props.data.label, (newLabel) => {
  if (newLabel && labelRef.value) {
    nextTick(() => {
      if (labelRef.value) {
        labelWidth.value = labelRef.value.offsetWidth + 10; // 텍스트 너비 + 여백
        labelHeight.value = labelRef.value.offsetHeight + 4; // 텍스트 높이 + 여백
      }
    });
  } else {
    labelWidth.value = 100; labelHeight.value = 24; // 라벨이 없으면 기본값으로
  }
}, { immediate: true }); // 컴포넌트 마운트 시 즉시 실행

// 선의 경로 (getBezierPath 사용)
const path = computed(() => getBezierPath({
  sourceX: props.sourceX,
  sourceY: props.sourceY,
  sourcePosition: props.sourcePosition,
  targetX: props.targetX,
  targetY: props.targetY,
  targetPosition: props.targetPosition,
}))

// [추가] 끝지점 화살표의 각도를 계산합니다.
const arrowAngle = computed(() => {
  const p1 = { x: props.sourceX, y: props.sourceY };
  const p2 = { x: props.targetX, y: props.targetY };
  
  // 시작점과 끝점 사이의 각도를 계산합니다.
  // 베지어 곡선의 경우 완벽하지 않지만, 방향을 확인하는 데는 충분합니다.
  const angle = Math.atan2(p2.y - p1.y, p2.x - p1.x) * 180 / Math.PI;
  return angle + 180;
});


// 라벨의 위치 계산 (오프셋 적용)
const labelStyle = computed(() => {
  const x = path.value[1] + (props.data.labelOffsetX || 0);
  const y = path.value[2] + (props.data.labelOffsetY || 0);
  return {
    // foreignObject의 위치를 라벨의 중앙으로 맞추기 위해 크기의 절반만큼 빼줍니다.
    transform: `translate(${x - labelWidth.value / 2}px, ${y - labelHeight.value / 2}px)`,
    cursor: 'move',
    // 라벨이 없을 때는 이벤트 방지
    pointerEvents: props.data.label ? 'all' : 'none',
  }
});


// 선 스타일 (점선/실선)
const edgeStyle = computed(() => ({
  stroke: '#555',
  strokeWidth: 2,
  // data.lineStyle이 'dashed'이면 점선 적용
  strokeDasharray: props.data.lineStyle === 'dashed' ? '5 5' : 'none',
}))

// --- 라벨 드래그 로직 ---
const isDragging = ref(false);

function onDragStart(event) {
  isDragging.value = true;

  // 드래그 중 텍스트 선택 방지
  event.preventDefault();

  // 현재 마우스 위치와 라벨 오프셋을 기록
  const startPos = { x: event.clientX, y: event.clientY };
  const startOffsets = { x: props.data.labelOffsetX || 0, y: props.data.labelOffsetY || 0 };

  // [수정] onPaneMouseMove 대신 표준 이벤트 리스너를 사용합니다.
  const onMouseMove = (moveEvent) => {
    if (!isDragging.value) return;

    // 시작점으로부터의 마우스 이동 거리 계산
    const dx = moveEvent.clientX - startPos.x;
    const dy = moveEvent.clientY - startPos.y;

    // 뷰포트의 줌 레벨을 고려하여 이동 거리 보정 (project 함수는 Vue Flow 인스턴스에서 가져옴)
    const { zoom } = project({ x: 0, y: 0 });

    // 새 오프셋 계산
    const newOffsetX = startOffsets.x + dx / zoom;
    const newOffsetY = startOffsets.y + dy / zoom;

    emit('update:data', { ...props.data, labelOffsetX: newOffsetX, labelOffsetY: newOffsetY });
  };

  // [수정] onPaneMouseUp 대신 표준 이벤트 리스너를 사용합니다.
  const onMouseUp = () => {
    isDragging.value = false;
    // 마우스 이벤트 리스너를 window에서 제거합니다.
    window.removeEventListener('mousemove', onMouseMove);
    window.removeEventListener('mouseup', onMouseUp);
  };

  // window에 이벤트 리스너를 등록합니다.
  window.addEventListener('mousemove', onMouseMove);
  window.addEventListener('mouseup', onMouseUp);
}
</script>

<style>
.vue-flow__edge-arrow {
  fill: #555; /* [수정] 선 색상과 동일하게 설정 */
  stroke: #555;
}
</style>
<style scoped>
/* [수정] foreignObject 안에 들어갈 라벨 배경 스타일 */
.edge-label-bg {
  display: inline-block;
  padding: 2px 5px;
  background: #f8f9fa; /* 캔버스 배경색(VueFlowClassTest.vue)과 일치 */
  color: #333;
  font-size: 13px;
  font-weight: 500;
  border-radius: 4px;
  text-align: center;
  /* foreignObject 중앙 정렬을 위한 스타일 */
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  white-space: nowrap;
}
</style>
