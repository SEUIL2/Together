<template>
  <v-line
    :config="{
      points: getDiamondPoints(),
      stroke: fill,
      strokeWidth: 1.5,
      closed: true,
      fill: isFilled ? fill : '',   // ✅ 조건 분기
      lineJoin: 'round'
    }"
/>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  x: Number,
  y: Number,
  fill: String,
  direction: String,
  filled: Boolean // ✅ 새로 추가된 prop: true면 꽉찬 다이아몬드
})

const offset = 6
const isFilled = computed(() => props.filled === true)

const offsetPosition = computed(() => {
  switch (props.direction) {
    case 'up': return { x: props.x, y: props.y - offset }
    case 'down': return { x: props.x, y: props.y + offset }
    case 'left': return { x: props.x - offset, y: props.y }
    case 'right': return { x: props.x + offset, y: props.y }
    default: return { x: props.x, y: props.y }
  }
})

const size = 7
const getDiamondPoints = () => {
  const { x, y } = offsetPosition.value
  const dir = props.direction

  if (dir === 'up' || dir === 'down') {
    return [
      x, y - size,
      x + size, y,
      x, y + size,
      x - size, y
    ]
  } else {
    return [
      x - size, y,
      x, y - size,
      x + size, y,
      x, y + size
    ]
  }
}
</script>
