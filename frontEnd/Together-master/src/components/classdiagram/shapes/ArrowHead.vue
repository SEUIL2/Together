<template>
  <!-- 화살표 방향 및 위치 보정 적용 -->
  <v-shape
    :sceneFunc="draw"
    :config="{
      x: offsetPosition.x,
      y: offsetPosition.y,
      rotation,
      fill
    }"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  x: Number,
  y: Number,
  direction: String,
  fill: String
})

// 🔁 회전 각도 조정 (방향 보정)
const rotation = computed(() => {
  switch (props.direction) {
    case 'right': return 180   // 원래는 0 → 반대 방향
    case 'down': return -90    // 원래는 90
    case 'left': return 0      // 원래는 180
    case 'up': return 90       // 원래는 -90
    default: return 0
  }
})


// ✅ 침투 방지를 위한 위치 보정
const offset = 0
const offsetPosition = computed(() => {
  switch (props.direction) {
    case 'up': return { x: props.x, y: props.y - offset }
    case 'down': return { x: props.x, y: props.y + offset }
    case 'left': return { x: props.x - offset, y: props.y }
    case 'right': return { x: props.x + offset, y: props.y }
    default: return { x: props.x, y: props.y }
  }
})

// 🔺 커스텀 화살표 도형
function draw(context, shape) {
  const { fill } = shape.getAttrs()
  context.beginPath()
  context.moveTo(0, 0)
  context.lineTo(-10, -6)
  context.lineTo(-6, 0)
  context.lineTo(-10, 6)
  context.closePath()
  context.fillStyle = fill || '#2c3e50'
  context.fill()
  context.strokeStyle = '#2c3e50'
  context.lineWidth = 1.5
  context.stroke()
}
</script>
