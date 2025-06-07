<template>
  <v-regular-polygon
    :config="{
      x: offsetPosition.x,
      y: offsetPosition.y,
      sides: 3,
      radius: 10,
      fill,
      rotation: getRotation(direction)
    }"
/>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  x: Number,
  y: Number,
  fill: String,
  direction: String
})

// 🔁 박스 침투 방지용 오프셋 적용
const offset = 8
const offsetPosition = computed(() => {
  switch (props.direction) {
    case 'up': return { x: props.x, y: props.y - offset }
    case 'down': return { x: props.x, y: props.y + offset }
    case 'left': return { x: props.x - offset, y: props.y }
    case 'right': return { x: props.x + offset, y: props.y }
    default: return { x: props.x, y: props.y }
  }
})

// 🔁 방향 회전 보정 (from → to 기준)
const getRotation = (dir) => {
  switch (dir) {
    case 'up': return 180
    case 'down': return 0
    case 'left': return 90
    case 'right': return 270
    default: return 0
  }
}
</script>
