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
  context.moveTo(0, 0);         // 정점
  context.lineTo(-10, -6);      // 좌하단
  context.lineTo(-10, 6);       // 우하단
  context.closePath();
  context.strokeStyle = '#333';
  context.lineWidth = 2;
  context.stroke();
}
</script>
