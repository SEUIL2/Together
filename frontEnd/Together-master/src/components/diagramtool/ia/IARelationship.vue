<template>
  <v-group
    @contextmenu="handleRightClick"
    @dblclick="handleLineDblClick"
    @mouseenter="isHovered = true"
    @mouseleave="isHovered = false"
  >
    <!-- 관계 꺾인 선 -->
    <v-line
      :points="allPoints"
      :stroke="strokeColor"
      :strokeWidth="1.5"
      :hitStrokeWidth="16"
      :dash="dashPattern"
      lineCap="round"
      lineJoin="round"
      :listening="true"
    />

    <!-- 끝단 화살표 -->
    <v-arrow
      :points="arrowPoints"
      :pointerLength="10"
      :pointerWidth="8"
      :fill="strokeColor"
      :stroke="strokeColor"
    />

    <!-- 중간점: 드래그/삭제(더블클릭) -->
<v-circle
      v-for="(mid, idx) in midPoints"
      :key="idx"
      :x="mid.x"
      :y="mid.y"
      :radius="isHovered ? 5 : 4"
      :fill="strokeColor"
      :opacity="isHovered ? 1 : 0.5"
      :strokeWidth="isHovered ? 6 : 0"
      stroke="#007bff44"
      :draggable="true"
      :dragCursor="'move'"
      @dragmove="e => emit('update-mid-point', { rel, idx, x: e.target.x(), y: e.target.y() })"
      @dragend="() => emit('mid-drag-end', rel)"
      @dblclick="e => handleMidDblClick(e, idx)"
    />
  </v-group>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  from: Object, to: Object,
  midPoints: Array,
  lineStyle: String,
  rel: Object,
})
const emit = defineEmits([
  'open-context',      // 우클릭 메뉴 (삭제 등)
  'add-mid-point',     // 중간점 추가
  'update-mid-point',  // 중간점 이동
  'delete-mid-point',  // 중간점 삭제
  'mid-drag-end',      // 중간점 드래그 끝
])

const isHovered = ref(false)

const strokeColor = '#555'
const dashPattern = computed(() =>
  props.lineStyle === 'dashed' ? [6, 4] : []
)
const midPoints = computed(() => props.midPoints || [])

// [from, ...mids..., to]
const allPoints = computed(() => {
  const safeCoord = (point) =>
    (point && Number.isFinite(point.x) && Number.isFinite(point.y))
      ? [point.x, point.y]
      : [0, 0]
  const from = safeCoord(props.from)
  const to = safeCoord(props.to)
  const mids = midPoints.value.flatMap(p =>
    Number.isFinite(p?.x) && Number.isFinite(p?.y) ? [p.x, p.y] : []
  )
  return [...from, ...mids, ...to]
})
const arrowPoints = computed(() => {
  const pts = allPoints.value
  const len = pts.length
  return [pts[len - 4], pts[len - 3], pts[len - 2], pts[len - 1]]
})

// 우클릭 → 삭제 메뉴
function handleRightClick(e) {
  if (e.evt) e.evt.preventDefault()
  emit('open-context', {
    rel: props.rel,
    x: e.evt?.clientX,
    y: e.evt?.clientY
  })
}

// 선 더블클릭 → 중간점 추가
function handleLineDblClick(e) {
  // Konva 이벤트: e.target이 v-circle인지 확인해서, v-line에서만 동작하게!
  if (e.target && e.target.className && e.target.className === 'Circle') {
    // 중간점에서 발생한 더블클릭은 무시
    return;
  }
  if (e.evt) {
    e.evt.preventDefault()
    e.evt.stopPropagation()
  }
  const stage = e.target.getStage()
  const pointer = stage.getPointerPosition()
  if (pointer) {
    emit('add-mid-point', {
      rel: props.rel,
      x: pointer.x,
      y: pointer.y
    })
  }
}


// 중간점 더블클릭 → 해당 중간점 삭제
function handleMidDblClick(e, idx) {
  if (e.evt) {
    e.evt.stopPropagation();
    e.evt.preventDefault();
    e.evt.cancelBubble = true;
  }
  emit('delete-mid-point', { rel: props.rel, idx })
}
</script>
