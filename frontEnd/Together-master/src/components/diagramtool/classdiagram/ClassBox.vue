<template>
  <v-group
    :key="config.id + '-' + config.attributes.length + '-' + config.methods.length"
    :config="{ x: config.x, y: config.y, draggable: true }"
    @dragend="onDragEnd"
    @contextmenu="onRightClick"
    @mouseenter="showAnchors = true"
    @mouseleave="showAnchors = false"
  >
    <!-- 박스 배경 -->
    <v-rect
      :config="{
        width: config.width,
        height: calculateHeight,
        fill: '#ffffff',
        stroke: '#e1e4e8',
        strokeWidth: 1.5,
        cornerRadius: 10,
        shadowColor: 'rgba(0,0,0,0.08)',
        shadowBlur: 12,
        shadowOffset: { x: 2, y: 2 },
        shadowOpacity: 0.4
      }"
    />

    <!-- Header Background -->
    <v-rect
      :config="{
        width: config.width,
        height: headerHeight,
        fill: '#f1f5f9',
        cornerRadius: [10, 10, 0, 0],
      }"
    />
    <v-line
      :config="{
        points: [0, headerHeight, config.width, headerHeight],
        stroke: '#e1e4e8',
        strokeWidth: 1.5,
      }"
    />

    <!-- 클래스명 -->
    <v-text
      :config="{
        text: config.name,
        fontSize: 16,
        fontStyle: 'bold',
        x: 0,
        y: 0,
        width: config.width,
        height: headerHeight,
        align: 'center',
        verticalAlign: 'middle',
        fill: '#1e293b',
        listening: true
      }"
      @dblclick="() => emitStartEdit('name', null, config.name, 10, 14)"
    />

    <!-- 속성 목록 -->
    <v-text
      v-for="(attr, i) in config.attributes"
      :key="'attr-' + attr + '-' + i"
      :config="{
        text: attr,
        x: 10,
        y: attributesStartY + i * attrLineHeight,
        fontSize: 14,
        fill: '#475569',
        listening: true
      }"
      @dblclick="() => emitStartEdit('attribute', i, attr, 10, attributesStartY + i * attrLineHeight)"
    />
    <!-- 속성 + 버튼 (항상 속성 아래 8px 여백) -->
    <v-text
      :config="{
        text: '+ 속성 추가',
        x: 10,
        y: attrAddBtnY,
        fontSize: 13,
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
        stroke: '#e1e4e8',
        strokeWidth: 1.5
      }"
    />

    <!-- 메서드 목록 -->
    <v-text
      v-for="(method, j) in config.methods"
      :key="'method-' + method + '-' + j"
      :config="{
        text: method,
        x: 10,
        y: methodsStartY + j * attrLineHeight,
        fontSize: 14,
        fill: '#0f766e',
        listening: true
      }"
      @dblclick="() => emitStartEdit('method', j, method, 10, methodsStartY + j * attrLineHeight)"
    />
    <!-- 메서드 + 버튼 (메서드 목록 아래 10px 여백) -->
    <v-text
      :config="{
        text: '+ 메서드 추가',
        x: 10,
        y: methodAddBtnY,
        fontSize: 13,
        fill: '#27ae60',
        listening: true,
        fontStyle: 'bold',
        cursor: 'pointer'
      }"
      @click="addMethod"
    />

    <!-- Anchor 포인트 (상/하/좌/우) -->
    <v-circle
      v-for="anchor in anchors"
      :key="'anchor-' + anchor.direction"
      :config="{
        x: anchor.x,
        y: anchor.y,
        radius: 5,
        fill: '#4ba7fa',
        stroke: '#fff',
        strokeWidth: 2,
        opacity: showAnchors ? 1 : 0,
        draggable: false
      }"
      @click="$emit('anchor-click', {
        boxId: config.id,
        direction: anchor.direction,
        x: config.x + anchor.x,
        y: config.y + anchor.y
      })"
      @mouseenter="e => e.target.getStage().container().style.cursor = 'crosshair'"
      @mouseleave="e => e.target.getStage().container().style.cursor = 'default'"
    />
  </v-group>
</template>

<script setup>
import { toRefs, computed, ref } from 'vue'

const props = defineProps({
  config: Object,
  isEditing: Boolean,
})
const emit = defineEmits([
  'update-position', 'anchor-click', 'start-edit', 'contextmenu',
  'update-attribute', 'update-method', 'update-name',
  'delete-attribute', 'delete-method'
])
const { config } = toRefs(props)
const showAnchors = ref(false)

// 레이아웃 상수
const headerHeight = 44         // 헤더 높이
const attrLineHeight = 22       // 속성, 메서드 줄간격
const attrAddBtnGap = 8         // 속성 +버튼 위 여백
const attrAddBtnHeight = 18     // +속성 추가 높이 + 여유
const methodSectionGap = 10     // 구분선 아래 첫 메서드까지 여백
const methodAddBtnHeight = 20   // +버튼 높이+여백
const sectionVPadding = 12      // 섹션 상하 여백

// 각 섹션 위치 계산(computed)
const attributesStartY = computed(() => headerHeight + sectionVPadding)

const attrAddBtnY = computed(() =>
  attributesStartY.value + config.value.attributes.length * attrLineHeight + attrAddBtnGap
)

const attrSectionBottom = computed(() =>
  attrAddBtnY.value + attrAddBtnHeight
)

const methodsStartY = computed(() =>
  attrSectionBottom.value + methodSectionGap
)

const methodAddBtnY = computed(() =>
  methodsStartY.value + config.value.methods.length * attrLineHeight
)

// 전체 박스 높이 계산 (메서드 + 버튼까지 포함 + 아래 여유)
const minBoxHeight = 80
const calculateHeight = computed(() => {
  return Math.max(methodAddBtnY.value + methodAddBtnHeight + sectionVPadding, minBoxHeight)
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
