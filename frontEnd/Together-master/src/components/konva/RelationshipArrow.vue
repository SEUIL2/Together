<template>
  <v-group @contextmenu="onRightClick" @dblclick="onLineDblClick">
    <!-- 메인 선 -->
    <v-line
      :points="allPoints"
      :stroke="strokeColor"
      :strokeWidth="2"
      :hitStrokeWidth="16"
      :dash="lineStyle === 'dashed' ? [6, 4] : []"
      lineCap="round"
      lineJoin="round"
    />

    <!-- 시작 머리 -->
    <component
      v-if="fromType !== 'none'"
      :is="getHeadComp(fromType)"
      :x="from.x"
      :y="from.y"
      :rotation="startAngleDeg"
      :offset="0"
      :fill="strokeColor"
      :filled="fromType === 'filled_diamond'"
    />

    <!-- 끝 머리 -->
    <component
      v-if="toType !== 'none'"
      :is="getHeadComp(toType)"
      :x="to.x"
      :y="to.y"
      :rotation="endAngleDeg"
      :offset="0"
      :fill="strokeColor"
      :filled="toType === 'filled_diamond'"
    />

    <!-- 꺾임점 -->
    <v-circle
      v-for="(m, i) in midPoints"
      :key="i"
      :x="m.x" :y="m.y"
      :radius="4"
      fill="#fff"
      stroke="#555"
      :strokeWidth="2"
      :draggable="true"
      dragCursor="move"
      @dragmove="e => emit('update-mid-point', { rel, idx: i, x: e.target.x(), y: e.target.y() })"
      @dragend="() => emit('mid-drag-end', rel)"
      @dblclick="() => emit('delete-mid-point', { rel, idx: i })"
    />
  </v-group>
</template>

<script setup>
import { computed } from 'vue'
import TriangleHead from '../classdiagram/shapes/TriangleHead.vue'
import ArrowHead    from '../classdiagram/shapes/ArrowHead.vue'
import DiamondHead  from '../classdiagram/shapes/DiamondHead.vue'
import EmptyHead    from '../classdiagram/shapes/EmptyHead.vue'

const props = defineProps({
  from:      { type: Object, required: true }, // {x,y} stage 좌표
  to:        { type: Object, required: true },
  fromType:  { type: String, default: 'none' }, // 'arrow'|'triangle'|'empty_diamond'|'filled_diamond'|'none'
  toType:    { type: String, default: 'none' },
  lineStyle: { type: String, default: 'solid' },
  midPoints: { type: Array,  default: () => [] },
  rel:       { type: Object, default: () => ({}) }
})
const emit = defineEmits(['open-context','add-mid-point','update-mid-point','delete-mid-point','mid-drag-end'])

const strokeColor = '#333'
const midPoints = computed(() => props.midPoints || [])
const nodes = computed(() => [props.from, ...midPoints.value, props.to])

function getHeadComp(t){
  switch (t) {
    case 'triangle':       return TriangleHead
    case 'arrow':          return ArrowHead
    case 'empty_diamond':
    case 'filled_diamond': return DiamondHead
    default:               return EmptyHead
  }
}
function headLen(t){
  switch (t) {
   case 'triangle':       return 14   // ✅ radius(=10) * 1.5 = 15
   case 'arrow':          return 10  // ArrowHead:    끝점 x = -10
   case 'empty_diamond':
   case 'filled_diamond': return 20  // DiamondHead:  끝점 x = -20
    default:               return 0
  }
}

function v(a,b){ return {x:b.x-a.x, y:b.y-a.y} }
function n(vv){ const L=Math.hypot(vv.x,vv.y)||1; return {x:vv.x/L, y:vv.y/L} }
function moveAlong(p0,p1,off){ const d=n(v(p0,p1)); return {x:p0.x+d.x*off, y:p0.y+d.y*off} }
function pullBack (p0,p1,off){ const d=n(v(p0,p1)); return {x:p1.x-d.x*off, y:p1.y-d.y*off} }

const firstKnee = computed(() => nodes.value[1] ?? props.to)
const lastKnee  = computed(() => nodes.value[nodes.value.length-2] ?? props.from)

function angleDeg(a,b){ return Math.atan2(b.y-a.y, b.x-a.x) * 180 / Math.PI }

const startHeadLen = computed(() => headLen(props.fromType))
const endHeadLen   = computed(() => headLen(props.toType))

// 선 길이 보정(머리만큼 당겨서 겹침 방지)
const allPoints = computed(() => {
  const pts = [...nodes.value]
  if (pts.length < 2) return []
  if (startHeadLen.value > 0) pts[0] = moveAlong(pts[0], pts[1], startHeadLen.value)
  if (endHeadLen.value   > 0) {
    const last = pts.length-1
    pts[last] = pullBack(pts[last-1], pts[last], endHeadLen.value)
  }
  const flat = []
  for (const p of pts) flat.push(p.x, p.y)
  return flat
})

// 연속 각도 (머리 컴포넌트에서 180° 모델 보정 적용)
 const startAngleDeg = computed(() => angleDeg(props.from, firstKnee.value))      // out
 const endAngleDeg   = computed(() => angleDeg(props.to,   lastKnee.value))       // in

function onRightClick(e){
  e.evt.preventDefault(); e.evt.stopPropagation()
  emit('open-context', { rel: props.rel, x: e.evt.clientX, y: e.evt.clientY })
}
function onLineDblClick(e){
  if (e.target.getClassName() === 'Circle') return
  e.evt.preventDefault(); e.evt.stopPropagation()
  const pos = e.target.getStage().getPointerPosition()
  if (pos) emit('add-mid-point', { rel: props.rel, x: pos.x, y: pos.y })
}
</script>
