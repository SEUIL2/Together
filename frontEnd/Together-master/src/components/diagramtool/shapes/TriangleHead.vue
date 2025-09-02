<template>
  <!-- (x,y)가 삼각형 팁(앵커). 그룹 회전으로 선 각도에 맞춤 -->
  <v-group :x="x" :y="y" :rotation="angleDeg">
    <v-shape
      :sceneFunc="draw"
      :fill="filled ? fill : '#fff'"
      :stroke="fill"
      :strokeWidth="1.5"
      :listening="false"
    />
  </v-group>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  x: Number,                // 앵커 x (팁)
  y: Number,                // 앵커 y (팁)
  fill: { type: String, default: '#2c3e50' },

  // 연속 각도 우선, 없으면 direction을 매핑
  rotation: { type: Number, default: null },
  direction: { type: String, default: null }, // 'up'|'down'|'left'|'right'

  // 삼각형 길이: 팁 → 밑변 중심까지의 높이(h)
  // → RelationshipArrow의 headLen('triangle')와 동일해야 선과 딱 맞음
  length: { type: Number, default: 15 },

  // 빈/채움 선택
  filled: { type: Boolean, default: true },

  // 미세 위치 보정(라인 진행 방향으로 ±, 보통 0)
  nudge: { type: Number, default: 0 }
})

function dirToDeg(d) {
  switch (d) {
    case 'right': return 0
    case 'down':  return 90
    case 'left':  return 180
    case 'up':    return -90
    default:      return 0
  }
}

 const angleDeg = computed(() => {
   const base = props.rotation ?? dirToDeg(props.direction)
   return (base + 180) % 360   // 180도 반전
})

// 정삼각형: 밑변의 반쪽 길이 = h / √3
const halfBase = computed(() => props.length / Math.sqrt(3))

function draw(ctx, shape) {
  const h  = props.length + props.nudge
  const hb = halfBase.value

  ctx.beginPath()
  // (0,0)이 팁(선 방향 = +X)
  ctx.moveTo(0, 0)
  ctx.lineTo(-h, -hb)
  ctx.lineTo(-h,  hb)
  ctx.closePath()

  // ★ 반드시 호출해야 실제로 그려짐
  ctx.fillStrokeShape(shape)
}
</script>
