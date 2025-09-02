<!-- src/components/classdiagram/shapes/DiamondHead.vue -->
<template>
  <!-- 팁(0,0)이 선 끝점인 좌표계로 그려놓고 그룹 회전 -->
  <v-group :x="tipX" :y="tipY" :rotation="angleDeg">
    <v-line
      :points="diamondPoints"
      :stroke="fill"
      :strokeWidth="1.5"
      :closed="true"
      :fill="isFilled ? fill : undefined"
      lineJoin="round"
    />
  </v-group>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  x: Number,                  // 선 끝점(앵커) x
  y: Number,                  // 선 끝점(앵커) y
  fill: { type: String, default: '#2c3e50' },
  /** rotation(연속 각도, 도 단위)이 우선, 없으면 direction을 매핑 */
  rotation: { type: Number, default: null },
  direction: { type: String, default: null }, // 'up'|'down'|'left'|'right'
  filled: { type: Boolean, default: false },  // 합성(채움) / 집합(빈 다이아)
  /** 선에서 뒤로 당길 길이. (보통 0; 선에서 이미 headLen만큼 당겼다면 이 값은 0 유지) */
  offset: { type: Number, default: 0 }
})

const isFilled = computed(() => props.filled === true)

function dirToDeg(d) {
  switch (d) {
    case 'right': return 0
    case 'down':  return 90
    case 'left':  return 180
    case 'up':    return -90
    default:      return 0
  }
}

/** 이 도형은 0° 기준이 '왼쪽'으로 그려진 모델이라면 +180 보정이 필요.
 *  (우리 Arrow/Triangle와 맞추려면 동일하게 180을 주는 게 안전)
 */
const MODEL_OFFSET = 180

// 최종 회전 각도(도)
const angleDeg = computed(() => {
  const base = props.rotation != null ? props.rotation : dirToDeg(props.direction)
  return base + MODEL_OFFSET
})

// offset만큼 선에서 뒤로 당겨 팁 위치를 보정
const rad  = computed(() => (angleDeg.value * Math.PI) / 180)
const tipX = computed(() => props.x - Math.cos(rad.value) * props.offset)
const tipY = computed(() => props.y - Math.sin(rad.value) * props.offset)

/** 다이아몬드 형태 (기본: 오른쪽을 향한 좌표계에서 그린 후 MODEL_OFFSET으로 뒤집힘)
 *  팁(0,0) → 뒤쪽 꼭짓점들. 가장 뒤 꼭짓점 x = -20 이므로 headLen = 20 사용 권장
 */
const diamondPoints = computed(() => [
  0,   0,   // 팁
  -10, -7,
  -20,  0,
  -10,  7
])
</script>
