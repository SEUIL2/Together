<template>
  <v-group>
    <!-- 관계선 -->
    <v-line
      :points="allPoints"
      :stroke="getStrokeColor()"
      :strokeWidth="2"
      :hitStrokeWidth="16"
      :dash="lineStyle === 'dashed' ? [6, 4] : []"
      @contextmenu="onRightClick"
      @dblclick="onLineDblClick"      
    />
    <!-- 꺾임점 -->
    <v-circle
      v-for="(mid, idx) in midPoints"
      :key="idx"
      :x="mid.x"
      :y="mid.y"
      :radius="4"
      fill="white"
      stroke="#555"
      :strokeWidth="2"
      :draggable="true"
  :dragCursor="'move'"
      @dragmove="e => onMidDrag(e, idx)"
      @dragstart="() => emit('mid-drag-start')"    
      @dragend="() => emit('mid-drag-end')"       
      @dblclick="e => onMidDelete(idx)" 
    />

<v-image
  v-if="fromOuter"
  :image="getSymbolImage(fromOuter)"
  v-bind="getSymbolPosition('from', 'outer')"
  :width="24" :height="24"
  :offsetX="12"
  :offsetY="12"
  :rotation="getSymbolRotation('from', 'outer', fromOuter)"
  :draggable="false"
/>
<v-image
  v-if="fromInner"
  :image="getSymbolImage(fromInner)"
  v-bind="getSymbolPosition('from', 'inner')"
  :width="24" :height="24"
    :offsetX="12"
  :offsetY="12"
  :rotation="getSymbolRotation('from', 'inner', fromInner)"
  :draggable="false"
/>
<v-image
  v-if="toInner"
  :image="getSymbolImage(toInner)"
  v-bind="getSymbolPosition('to', 'inner')"
  :width="24" :height="24"
    :offsetX="12"
  :offsetY="12"
  :rotation="getSymbolRotation('to', 'inner', toInner)"
  :draggable="false"
/>
<v-image
  v-if="toOuter"
  :image="getSymbolImage(toOuter)"
  v-bind="getSymbolPosition('to', 'outer')"
  :width="24" :height="24"
    :offsetX="12"
  :offsetY="12"
  :rotation="getSymbolRotation('to', 'outer', toOuter)"
  :draggable="false"
/>


  </v-group>
</template>

<script setup>
import { computed } from 'vue'
// SVG 파일 import
import crowsfootSrc from '@/assets/erd-symbols/crowsfoot.svg'
import barSrc from '@/assets/erd-symbols/bar.svg'
import circleSrc from '@/assets/erd-symbols/circle.svg'
import arrowSrc from '@/assets/erd-symbols/arrow.svg'

const props = defineProps({
  from: Object,
  to: Object,
  midPoints: Array,
  lineStyle: String,
  rel: Object,
  fromOuter: String,
  fromInner: String,
  toInner: String,
  toOuter: String,
})

const emit = defineEmits([
  'open-context', 'add-mid-point', 'update-mid-point', 'delete-mid-point',
  'mid-drag-start', 'mid-drag-end'
])
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
// 전체 선 좌표 배열
const allPoints = computed(() => {
  const base = [props.from.x, props.from.y]
  const mids = (props.midPoints || []).flatMap(m => [m.x, m.y])
  return [...base, ...mids, props.to.x, props.to.y]
})

function getSymbolAngle(which) {
  if (which === 'from') {
    // 꺾임점 있으면 from → midPoints[0] / 없으면 from → to
    const a = props.from;
    const b = (props.midPoints?.length > 0) ? props.midPoints[0] : props.to;
    return Math.atan2(b.y - a.y, b.x - a.x) * 180 / Math.PI;
  } else {
    // 꺾임점 있으면 to → midPoints[last] / 없으면 to → from
    const a = props.to;
    const b = (props.midPoints?.length > 0) ? props.midPoints[props.midPoints.length - 1] : props.from;
    return Math.atan2(b.y - a.y, b.x - a.x) * 180 / Math.PI;
  }
}

function getSymbolRotation(which, pos, symbol) {
  let angle = getSymbolAngle(which);
  // Crow's Foot이면 180도 추가 (from, to 모두!)
  if (symbol === 'crowsfoot') {
    angle += 180;
  }
  return angle;
}


function getSymbolPosition(which, pos) {
  // 항상 박스 anchor 기준! (꺾임점 무시)
  const base = which === 'from' ? props.from : props.to;

  // base 좌표 안전성 보장
  const bx = Number.isFinite(base?.x) ? base.x : 0;
  const by = Number.isFinite(base?.y) ? base.y : 0;

  // from→to 각도 (항상 박스→박스)
  const angle = getSymbolAngle(which) * Math.PI / 180;

  // 각 조합별 offset (양수만!)
  let offset = 340;
  if (which === 'from' && pos === 'outer') offset = 30;
  else if (which === 'from' && pos === 'inner') offset = 10;
  else if (which === 'to' && pos === 'outer') offset = -10;
  else if (which === 'to' && pos === 'inner') offset = -30;

  // from: +offset, to: –offset 방향
  const dir = (which === 'from') ? 1 : -1;

  // 필요시 y축 미세 조정
  let Y_SHIFT = 0;
  // (필요시 조정)

  return {
    x: bx + Math.cos(angle) * offset * dir,
    y: by + Math.sin(angle) * offset * dir + Y_SHIFT,
  };
}



const onRightClick = (e) => {
  if (e.evt && typeof e.evt.preventDefault === 'function') e.evt.preventDefault()
  emit('open-context', { rel: props.rel, event: e.evt })
}
const onLineDblClick = (e) => {
  if (e.evt) {
    const pos = e.target.getStage().getPointerPosition()
    emit('add-mid-point', { rel: props.rel, x: pos.x, y: pos.y })
  }
}
const onMidDrag = (e, idx) => {
  emit('update-mid-point', { rel: props.rel, idx, x: e.target.x(), y: e.target.y() })
}
const onMidDelete = (idx) => {
  emit('delete-mid-point', { rel: props.rel, idx })
}
const getStrokeColor = () => '#333'
</script>
