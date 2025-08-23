<template>
  <v-shape :sceneFunc="draw" :config="{ x: tipX, y: tipY, rotation: angleDeg, fill }" />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  x: Number, y: Number,
  rotation:  { type: Number, default: 0 },     // 선 각도(°)
  fill:      { type: String, default: '#2c3e50' },
  offset:    { type: Number, default: 0 }      // 선에서 뒤로 당길 길이(px)
})

// 모델 보정: 이 도형은 0°에 '왼쪽'을 향해 그려졌다고 가정 → +180°
const MODEL_OFFSET = 180
const angleDeg = computed(() => props.rotation + MODEL_OFFSET)

const rad  = computed(() => (angleDeg.value * Math.PI) / 180)
const tipX = computed(() => props.x - Math.cos(rad.value) * props.offset)
const tipY = computed(() => props.y - Math.sin(rad.value) * props.offset)

function draw(ctx, shape){
  const { fill } = shape.getAttrs()
  ctx.beginPath()
  // (0,0) = 화살촉, 기본은 오른쪽을 향한 형태로 그려두되 MODEL_OFFSET으로 뒤집힘
  ctx.moveTo(0, 0)
  ctx.lineTo(-10, -6)
  ctx.lineTo(-6, 0)
  ctx.lineTo(-10, 6)
  ctx.closePath()
  ctx.fillStyle = fill; ctx.fill()
  ctx.strokeStyle = fill; ctx.lineWidth = 1.5; ctx.stroke()
}
</script>
