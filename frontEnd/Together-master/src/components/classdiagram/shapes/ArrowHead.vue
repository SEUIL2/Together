<template>
  <v-shape :sceneFunc="draw" :config="{ x, y, rotation }" />
</template>

<script setup>
const props = defineProps({
  x: Number,
  y: Number,
  rotation: Number
});

function draw(context, shape) {
  context.beginPath();
  context.moveTo(0, 0);         // 끝점
  context.lineTo(-10, -6);      // 좌측
  context.lineTo(-8, 0);        // 중간 돌출
  context.lineTo(-10, 6);       // 우측
  context.closePath();
  context.fillStyle = '#333';
  context.fill();
}
</script>
