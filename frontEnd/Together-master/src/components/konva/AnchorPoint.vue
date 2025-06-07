<template>
  <v-line
    :config="{
      points: [from.x, from.y, to.x, to.y],
      stroke: '#333',
      strokeWidth: 2,
      lineCap: 'round',
      dash: type === 'dependency' ? [5, 5] : [],
      pointerLength: 10,
      pointerWidth: 10
    }"
  />
</template>

<script setup>
const props = defineProps({
  from: Object, // { x, y }
  to: Object,   // { x, y }
  type: String  // 예: 'association', 'dependency'
})
</script>
