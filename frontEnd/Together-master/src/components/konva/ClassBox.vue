<template>
  <v-group
    :key="config.id + '-' + config.attributes.length + '-' + config.methods.length"
    :config="{ x: config.x, y: config.y, draggable: true }"
    @dragend="onDragEnd"
    @contextmenu="onRightClick"
  >
    <!-- 박스 배경 -->
    <v-rect
      :config="{
        width: config.width,
        height: calculateHeight,
        fill: '#fff',
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

    <!-- 속성 목록 -->
    <v-text
      v-for="(attr, i) in config.attributes"
      :key="'attr-' + attr + '-' + i"
      :config="{
        text: attr,
        x: 10,
        y: nameSectionBottom + i * attrLineHeight,
        fontSize: 14,
        fill: '#34495e',
        listening: true
      }"
      @dblclick="() => emitStartEdit('attribute', i, attr, 10, nameSectionBottom + i * attrLineHeight)"
    />
    <!-- 속성 + 버튼 (항상 속성 아래 8px 여백) -->
    <v-text
      :config="{
        text: '+ 속성 추가',
        x: 10,
        y: attrAddBtnY,
        fontSize: 12,
        fill: '#27ae60',
        listening: true,
        fontStyle: 'bold',
        cursor: 'pointer'
      }"
      @click="addAttribute"
    />

    <!-- 구분선 (속성+ 추가 버튼 아래 18px) -->
    <v-line
      :config="{
        points: [0, attrSectionBottom, config.width, attrSectionBottom],
        stroke: '#bdc3c7',
        strokeWidth: 1
      }"
    />

    <!-- 메서드 목록 -->
    <v-text
      v-for="(method, j) in config.methods"
      :key="'method-' + method + '-' + j"
      :config="{
        text: method,
        x: 10,
        y: attrSectionBottom + methodSectionGap + j * attrLineHeight,
        fontSize: 14,
        fill: '#2980b9',
        listening: true
      }"
      @dblclick="() => emitStartEdit('method', j, method, 10, attrSectionBottom + methodSectionGap + j * attrLineHeight)"
    />
    <!-- 메서드 + 버튼 (메서드 목록 아래 10px 여백) -->
    <v-text
      :config="{
        text: '+ 메서드 추가',
        x: 10,
        y: methodAddBtnY,
        fontSize: 12,
        fill: '#27ae60',
        listening: true,
        fontStyle: 'bold',
        cursor: 'pointer'
      }"
      @click="addMethod"
    />

    <!-- Anchor 포인트 (상/하/좌/우) -->
    <v-circle
      v-for="(anchor, index) in anchors"
      :key="'anchor-' + anchor.direction"
      :config="{
        x: anchor.x,
        y: anchor.y,
        radius: 6,
        fill: '#fff',
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
const emit = defineEmits([
  'update-position', 'anchor-click', 'start-edit', 'contextmenu',
  'update-attribute', 'update-method', 'update-name',
  'delete-attribute', 'delete-method'
])
const { config } = toRefs(props)

// 레이아웃 상수
const nameSectionHeight = 30    // 클래스명 아래 여백
const attrLineHeight = 22       // 속성, 메서드 줄간격
const attrAddBtnGap = 8         // 속성 +버튼 위 여백
const attrAddBtnHeight = 18     // +속성 추가 높이 + 여유
const methodSectionGap = 10     // 구분선 아래 첫 메서드까지 여백
const methodAddBtnHeight = 20   // +버튼 높이+여백

// 각 섹션 위치 계산(computed)
const nameSectionBottom = computed(() => nameSectionHeight)
const attrAddBtnY = computed(() =>
  nameSectionBottom.value + config.value.attributes.length * attrLineHeight + attrAddBtnGap
)
const attrSectionBottom = computed(() =>
  attrAddBtnY.value + attrAddBtnHeight
)
const methodAddBtnY = computed(() =>
  attrSectionBottom.value + methodSectionGap + config.value.methods.length * attrLineHeight
)

// 전체 박스 높이 계산 (메서드 + 버튼까지 포함 + 아래 여유)
const minBoxHeight = 80
const calculateHeight = computed(() => {
  return Math.max(methodAddBtnY.value + methodAddBtnHeight, minBoxHeight)
})

// Anchor 포인트 (계산 기반)
const anchors = computed(() => [
  { x: config.value.width / 2, y: 0, direction: 'top' },
  { x: config.value.width / 2, y: calculateHeight.value, direction: 'bottom' },
  { x: 0, y: calculateHeight.value / 2, direction: 'left' },
  { x: config.value.width, y: calculateHeight.value / 2, direction: 'right' }
])

// 핸들러
const addAttribute = () => {
  emit('update-attribute', { boxId: config.value.id, value: '' })
}
const addMethod = () => {
  emit('update-method', { boxId: config.value.id, value: '' })
}
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
const onDragEnd = (e) => {
  const pos = e.target.position()
  emit('update-position', {
    id: config.value.id,
    x: pos.x,
    y: pos.y
  })
}
const onRightClick = (e) => {
  emit('contextmenu', {
    event: e,
    id: config.value.id
  })
}
</script>
