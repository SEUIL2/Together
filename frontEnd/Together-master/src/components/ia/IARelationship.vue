<template>
  <v-group
    @contextmenu="handleRightClick"
    @dblclick="handleLineDblClick"
  >
    <!-- 관계 꺾인 선 -->
    <v-line
      :points="allPoints"
      :stroke="strokeColor"
      :strokeWidth="2"
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
  :radius="4"
  fill="#fff"
  stroke="#555"
  :strokeWidth="2"
  :draggable="true"
  :dragCursor="'move'"
  @dragmove="e => { 
  console.log('emit 전', idx, e.target.x(), e.target.y());
  emit('update-mid-point', { rel, idx, x: e.target.x(), y: e.target.y() })
}"

  @dragend="() => emit('mid-drag-end', rel)"
  @dblclick="e => handleMidDblClick(e, idx)"
/>


    <!-- 관계선 끝 심볼 (ERD 표기) -->
    <v-image
      v-if="fromOuter"
      :image="getSymbolImage(fromOuter)"
      v-bind="getSymbolPosition('from', 'outer')"
      :width="24" :height="24" :offsetX="12" :offsetY="12"
      :rotation="getSymbolRotation('from', 'outer', fromOuter)"
    />
    <v-image
      v-if="fromInner"
      :image="getSymbolImage(fromInner)"
      v-bind="getSymbolPosition('from', 'inner')"
      :width="24" :height="24" :offsetX="12" :offsetY="12"
      :rotation="getSymbolRotation('from', 'inner', fromInner)"
    />
    <v-image
      v-if="toInner"
      :image="getSymbolImage(toInner)"
      v-bind="getSymbolPosition('to', 'inner')"
      :width="24" :height="24" :offsetX="12" :offsetY="12"
      :rotation="getSymbolRotation('to', 'inner', toInner)"
    />
    <v-image
      v-if="toOuter"
      :image="getSymbolImage(toOuter)"
      v-bind="getSymbolPosition('to', 'outer')"
      :width="24" :height="24" :offsetX="12" :offsetY="12"
      :rotation="getSymbolRotation('to', 'outer', toOuter)"
    />
  </v-group>
</template>

<script setup>
import { computed } from 'vue'
import crowsfootSrc from '@/assets/erd-symbols/crowsfoot.svg'
import barSrc from '@/assets/erd-symbols/bar.svg'
import circleSrc from '@/assets/erd-symbols/circle.svg'
import arrowSrc from '@/assets/erd-symbols/arrow.svg'

const props = defineProps({
  from: Object, to: Object,
  midPoints: Array,
  lineStyle: String,
  rel: Object,
  fromOuter: String,
  fromInner: String,
  toOuter: String,
  toInner: String,
})
const emit = defineEmits([
  'open-context',      // 우클릭 메뉴 (삭제 등)
  'add-mid-point',     // 중간점 추가
  'update-mid-point',  // 중간점 이동
  'delete-mid-point',  // 중간점 삭제
  'mid-drag-end',      // 중간점 드래그 끝
])

const strokeColor = '#333'
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


// --- 심볼 이미지 로딩/계산 (ERD 도형) ---
const imageCache = {}
function loadImage(src) {
  if (imageCache[src]) return imageCache[src]
  const img = new window.Image()
  img.src = src
  imageCache[src] = img
  return img
}
function getSymbolImage(symbol) {
  switch (symbol) {
    case 'crowsfoot': return loadImage(crowsfootSrc)
    case 'bar': return loadImage(barSrc)
    case 'circle': return loadImage(circleSrc)
    case 'arrow': return loadImage(arrowSrc)
    default: return null
  }
}
function getSymbolAngle(which) {
  const a = (which === 'from') ? props.from : props.to
  const b = props.midPoints?.[which === 'from' ? 0 : props.midPoints.length - 1] || ((which === 'from') ? props.to : props.from)
  return Math.atan2(b.y - a.y, b.x - a.x) * 180 / Math.PI
}
function getSymbolRotation(which, pos, symbol) {
  let angle = getSymbolAngle(which)
  if (symbol === 'crowsfoot') angle += 180
  return angle
}
function getSymbolPosition(which, pos) {
  const base = which === 'from' ? props.from : props.to
  const angle = getSymbolAngle(which) * Math.PI / 180
  const offset = (which === 'from')
    ? (pos === 'outer' ? 30 : 10)
    : (pos === 'outer' ? -10 : -30)
  return {
    x: base.x + Math.cos(angle) * offset,
    y: base.y + Math.sin(angle) * offset
  }
}
</script>
