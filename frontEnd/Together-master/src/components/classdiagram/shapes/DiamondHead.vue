<template>
  <v-shape :sceneFunc="draw" :config="{ x, y, rotation }" />
</template>

<script setup>
const props = defineProps({
  x: Number,
  y: Number,
  rotation: Number,
  filled: Boolean
});

function draw(context, shape) {
  context.beginPath();
  context.moveTo(0, 0);
  context.lineTo(-6, -5);
  context.lineTo(-12, 0);
  context.lineTo(-6, 5);
  context.closePath();

  if (props.filled) {
    context.fillStyle = '#333';      // ✅ 채워질 색상
    context.fill();
  } else {
    context.lineWidth = 2;           // ✅ 선 두께
    context.strokeStyle = '#333';    // ✅ 선 색상
    context.stroke();
  }
}
</script>
