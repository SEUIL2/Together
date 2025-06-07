<template>
  <!-- 메인 선 -->
  <v-line
    :config="{
      points: getPoints(from, to, rel.bendStyle),
      stroke: '#2c3e50',
      strokeWidth: 2,
      dash: lineStyle === 'dashed' ? [6, 4] : [],
      lineCap: 'round',
      hitStrokeWidth: 10
    }"
    @click="handleClick"
    @contextmenu="handleContextMenu"
  />

  <!-- from 도형 -->
  <component
    v-if="fromType !== 'none'"
    :is="getArrowComponent(fromType)"
    :x="from.x"
    :y="from.y"
    :direction="getDirectionFrom(from, to, rel.bendStyle)"
    :fill="'#2c3e50'"
    :filled="fromType === 'diamond' ? rel.fromFilled : undefined"
  />

  <!-- to 도형 -->
  <component
    v-if="toType !== 'none'"
    :is="getArrowComponent(toType)"
    :x="to.x"
    :y="to.y"
    :direction="getDirectionTo(from, to, rel.bendStyle)"
    :fill="'#2c3e50'"
    :filled="toType === 'diamond' ? rel.toFilled : undefined"
  />
</template>

<script setup>
import TriangleHead from '../classdiagram/shapes/TriangleHead.vue'
import DiamondHead from '../classdiagram/shapes/DiamondHead.vue'
import ArrowHead from '../classdiagram/shapes/ArrowHead.vue'
import EmptyHead from '../classdiagram/shapes/EmptyHead.vue'

const props = defineProps({
  from: Object,
  to: Object,
  type: String,
  fromType: String,
  toType: String,
  lineStyle: String,
  rel: Object
})

const emit = defineEmits(['select'])

const handleClick = (e) => {
  emit('select', { rel: props.rel, event: e.evt })
}

const handleContextMenu = (e) => {
  e.evt.preventDefault()
  e.evt.stopPropagation()
  emit('select', { rel: props.rel, event: e.evt })
}

const getArrowComponent = (shape) => {
  switch (shape) {
    case 'triangle': return TriangleHead
    case 'diamond': return DiamondHead
    case 'arrow': return ArrowHead
    default: return EmptyHead
  }
}

const getDirectionFrom = (from, to, style = 'one') => {
  if (style === 'none') return getDirection(from, to)
  if (style === 'one') {
    const dx = to.x - from.x
    const dy = to.y - from.y
    return Math.abs(dx) > Math.abs(dy)
      ? dx > 0 ? 'right' : 'left'
      : dy > 0 ? 'down' : 'up'
  }
  if (style === 'two') {
    return to.x - from.x > 0 ? 'right' : 'left'
  }
  return getDirection(from, to)
}

const getDirectionTo = (from, to, style = 'one') => {
  if (style === 'none') return 'up' // 무조건 아래
  if (style === 'one') {
    const dx = to.x - from.x
    const dy = to.y - from.y
    return Math.abs(dx) > Math.abs(dy)
      ? dy > 0 ? 'up' : 'down'
      : dx > 0 ? 'left' : 'right'
  }
  if (style === 'two') {
    const dx = to.x - ((from.x + to.x) / 2)
    const dy = to.y - from.y
    return Math.abs(dx) > Math.abs(dy)
      ? dx > 0 ? 'left' : 'right'
      : dy > 0 ? 'up' : 'down'
  }
  return getDirection(from, to)
}

const getDirection = (from, to) => {
  const dx = to.x - from.x
  const dy = to.y - from.y
  const angle = Math.atan2(dy, dx) * (180 / Math.PI)
  if (angle >= -45 && angle < 45) return 'right'
  if (angle >= 45 && angle < 135) return 'down'
  if (angle >= 135 || angle < -135) return 'left'
  return 'up'
}

const getPoints = (from, to, style = 'one') => {
  if (style === 'none') return [from.x, from.y, to.x, to.y]
  if (style === 'one') {
    const dx = Math.abs(to.x - from.x)
    const dy = Math.abs(to.y - from.y)
    return dx > dy
      ? [from.x, from.y, to.x, from.y, to.x, to.y]
      : [from.x, from.y, from.x, to.y, to.x, to.y]
  }
  if (style === 'two') {
    const midX = (from.x + to.x) / 2
    return [from.x, from.y, midX, from.y, midX, to.y, to.x, to.y]
  }
  return [from.x, from.y, to.x, to.y]
}
</script>

<style scoped>
</style>
