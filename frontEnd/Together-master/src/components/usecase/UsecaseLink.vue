<template>
  <v-group>
    <!-- 선(실선/점선) + 화살표 -->
    <v-arrow
      :config="{
        points,
        pointerLength: 18,
        pointerWidth: 13,
        fill: arrowColor,
        stroke: arrowColor,
        strokeWidth: 2.5,
        dash: isDashed ? [8, 7] : [],
        lineCap: 'round',
        lineJoin: 'round'
      }"
      @contextmenu="onRightClick"
    />

    <!-- include/extend 텍스트 라벨 -->
    <v-text
      v-if="type === 'include' || type === 'extend'"
      :config="{
        text: labelText,
        x: labelPos.x,
        y: labelPos.y,
        fontSize: 14,
        fill: '#1976d2',
        fontStyle: 'bold'
      }"
    />
  </v-group>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  from: { type: Object, required: true }, // { x, y }
  to: { type: Object, required: true },   // { x, y }
  type: { type: String, default: 'include' }
})
const emit = defineEmits(['contextmenu'])

// **좌표 계산 - 더 이상 보정 안 함!**
const points = computed(() => [
  props.from.x, props.from.y, props.to.x, props.to.y
])

const isDashed = computed(() => props.type === 'extend')
const arrowColor = '#1976d2'

// 가운데 라벨 위치
const labelText = computed(() =>
  props.type === 'include' ? '<<include>>' :
  props.type === 'extend' ? '<<extend>>' : ''
)
const labelPos = computed(() => {
  const [x1, y1, x2, y2] = points.value
  return {
    x: (x1 + x2) / 2 - 36,
    y: (y1 + y2) / 2 - 20
  }
})

// 우클릭
const onRightClick = (e) => {
  emit('contextmenu', e)
}
</script>
