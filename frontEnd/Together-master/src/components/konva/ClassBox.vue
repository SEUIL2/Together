<template>
  <v-group
    :config="{ x: config.x, y: config.y, draggable: true }"
    @dragend="onDragEnd"
    @contextmenu="onRightClick"
  >
    <!-- 외곽 박스 -->
    <v-rect
      :config="{
        width: config.width,
        height: calculateHeight,
        fill: '#ffffff',
        stroke: '#2c3e50',
        strokeWidth: 2,
        cornerRadius: 6,
        shadowColor: 'rgba(0,0,0,0.2)',
        shadowBlur: 8,
        shadowOffset: { x: 2, y: 2 },
        shadowOpacity: 0.4
      }"
    />

    <!-- 클래스명 -->
    <v-text
      :config="{
        text: config.name,
        fontSize: 16,
        fontStyle: 'bold',
        padding: 8,
        fill: '#2c3e50',
        listening: true
      }"
      @dblclick="() => emitStartEdit('name', null, config.name, 10, 5)"
    />

    <!-- 속성 -->
    <v-text
      v-for="(attr, i) in config.attributes"
      :key="'attr-' + i"
      :config="{
        text: attr,
        x: 10,
        y: 30 + i * 18,
        fontSize: 14,
        fill: '#34495e',
        listening: true
      }"
      @dblclick="() => emitStartEdit('attribute', i, attr, 10, 30 + i * 18)"
    />

    <!-- 메서드 -->
    <v-text
      v-for="(method, j) in config.methods"
      :key="'method-' + j"
      :config="{
        text: method,
        x: 10,
        y: 40 + config.attributes.length * 18 + j * 18,
        fontSize: 14,
        fill: '#2980b9',
        listening: true
      }"
      @dblclick="() => emitStartEdit('method', j, method, 10, 40 + config.attributes.length * 18 + j * 18)"
    />

    <!-- 🔵 Anchor 포인트 (상/하/좌/우) -->
    <v-circle
      v-for="(anchor, index) in anchors"
      :key="'anchor-' + index"
      :config="{
        x: anchor.x,
        y: anchor.y,
        radius: 6,
        fill: '#ffffff',
        stroke: '#3498db',
        strokeWidth: 1.5,
        draggable: false
      }"
      @click="$emit('anchor-click', {
        boxId: config.id,
        direction: anchor.direction,
        x: config.x + anchor.x,
        y: config.y + anchor.y
      })"
    />
  </v-group>
</template>

<script setup>
import { toRefs, computed } from 'vue'

const props = defineProps({ config: Object })
const emit = defineEmits(['update-position', 'anchor-click', 'start-edit', 'contextmenu']) // ⬅️ 추가됨
const { config } = toRefs(props)

const onDragEnd = (e) => {
  const pos = e.target.position()
  emit('update-position', {
    id: config.value.id,
    x: pos.x,
    y: pos.y
  })
}

const calculateHeight = computed(() => {
  const attrHeight = config.value.attributes.length * 18
  const methodHeight = config.value.methods.length * 18
  return 50 + attrHeight + methodHeight
})

const anchors = computed(() => {
  const width = config.value.width
  const height = calculateHeight.value
  return [
    { x: width / 2, y: 0, direction: 'top' },
    { x: width / 2, y: height, direction: 'bottom' },
    { x: 0, y: height / 2, direction: 'left' },
    { x: width, y: height / 2, direction: 'right' }
  ]
})

const emitStartEdit = (type, index, value, offsetX, offsetY) => {
  emit('start-edit', {
    boxId: config.value.id,
    type,
    index,
    x: config.value.x + offsetX,
    y: config.value.y + offsetY,
    value
  })
}

const onRightClick = (e) => {
  emit('contextmenu', {
    event: e,
    id: config.value.id
  })
}
</script>

<style scoped>
</style>
