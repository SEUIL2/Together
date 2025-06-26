<!-- src/components/konva/RelationshipArrow.vue -->
<template>
  <v-group
    @contextmenu="onRightClick"
    @dblclick="onLineDblClick"
  >
    <!-- 메인 꺾인 선 -->
    <v-line
      :points="allPoints"
      :stroke="strokeColor"
      :strokeWidth="2"
      :hitStrokeWidth="16"
      :dash="lineStyle === 'dashed' ? [6, 4] : []"
      lineCap="round"
      lineJoin="round"
    />

    <!-- 시작 도형 -->
    <component
      v-if="fromType !== 'none'"
      :is="getArrowComponent(fromType)"
      :x="from.x"
      :y="from.y"
      :direction="getDirectionFrom(from, to, bendStyle)"
      :fill="strokeColor"
      :filled="fromType === 'filled_diamond'"
    />

    <!-- 끝 도형 -->
    <component
      v-if="toType !== 'none'"
      :is="getArrowComponent(toType)"
      :x="to.x"
      :y="to.y"
      :direction="getDirectionTo(from, to, bendStyle)"
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
  from:      Object,   // { x,y } from anchor
  to:        Object,   // { x,y } to anchor
  fromType:  String,   // 'none'|'arrow'|'triangle'|'empty_diamond'|'filled_diamond'
  toType:    String,
  lineStyle: String,   // 'solid'|'dashed'
  bendStyle: String,   // 'none'|'one'|'two'
  midPoints: Array,    // [{x,y},...]
  rel:       Object
})

const emit = defineEmits([
  'open-context',
  'add-mid-point',
  'update-mid-point',
  'delete-mid-point',
  'mid-drag-end'
])

// 선 색상
const strokeColor = '#333'

// 전체 포인트 계산 (bendStyle에 따라 원한다면 한/두 번 꺾이게 확장)
const allPoints = computed(() => {
  if (props.bendStyle === 'none') {
    return [props.from.x, props.from.y, props.to.x, props.to.y]
  }
  const pts = [props.from.x, props.from.y]
  props.midPoints?.forEach(p => { pts.push(p.x, p.y) })
  pts.push(props.to.x, props.to.y)
  return pts
})

// 꺾임점
const midPoints = computed(() => props.midPoints || [])

// 도형 컴포넌트 맵
function getArrowComponent(shape) {
  switch (shape) {
    case 'triangle':       return TriangleHead
    case 'arrow':          return ArrowHead
    case 'empty_diamond':  return DiamondHead
    case 'filled_diamond': return DiamondHead
    default:               return EmptyHead
  }
}

// 기본 방향 로직
function getDirection(a, b) {
  const dx = b.x - a.x, dy = b.y - a.y
  const ang = Math.atan2(dy, dx) * 180/Math.PI
  if (ang >= -45 && ang < 45)   return 'right'
  if (ang >= 45  && ang < 135)  return 'down'
  if (ang >= 135 || ang < -135) return 'left'
  return 'up'
}

// from/to 방향 계산
function getDirectionFrom(from, to, style='one') {
  if (style === 'none') return getDirection(from, to)
  if (style === 'one') {
    const dx = to.x-from.x, dy = to.y-from.y
    return Math.abs(dx)>Math.abs(dy) ? (dx>0?'right':'left') : (dy>0?'down':'up')
  }
  // style==='two' 등 추가 가능
  return getDirection(from, to)
}
function getDirectionTo(from, to, style='one') {
  if (style === 'none') return 'up'
  if (style === 'one') {
    const dx = to.x-from.x, dy = to.y-from.y
    return Math.abs(dx)>Math.abs(dy) ? (dy>0?'up':'down') : (dx>0?'left':'right')
  }
  return getDirection(to, from)
}

// 우클릭
function onRightClick(e) {
  e.evt.preventDefault()
  e.evt.stopPropagation()
  emit('open-context', { rel: props.rel, x: e.evt.clientX, y: e.evt.clientY })
}

// 선 더블클릭 → 꺾임점 추가
function onLineDblClick(e) {
  if (e.target.getClassName() === 'Circle') return
  e.evt.preventDefault()
  e.evt.stopPropagation()
  const pos = e.target.getStage().getPointerPosition()
  if (pos) emit('add-mid-point', { rel: props.rel, x: pos.x, y: pos.y })
}
</script>

<style scoped>
/* 필요시 추가 스타일 */
</style>
