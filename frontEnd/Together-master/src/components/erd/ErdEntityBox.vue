<template>
  <v-group
    :config="groupConfig"
    @mousedown="onMouseDown"
    @mouseup="onMouseUp"
    @dragmove="onDragMove"
    @click="onClick"
    @contextmenu="onContextMenu"
  >
    <!-- 박스 배경 -->
    <v-rect :config="rectConfig" />

    <!-- 테이블명 (더블클릭으로 편집) -->
    <v-text
      :config="tableNameConfig"
      style="cursor: text"
      @dblclick="onNameDblClick"
      listening
    />

    <!-- 필드 목록 -->
    <template v-for="(field, i) in props.entity.fields" :key="field.id">
      <v-text
        :config="getFieldConfig(i, field)"
        style="cursor: text"
        @dblclick="onFieldDblClick(i, $event)"
        listening
      />
      <!-- 필드 삭제 버튼 -->
      <v-text
        :config="getDeleteConfig(i)"
        style="cursor: pointer"
        @click="onRemoveField(i, $event)"
        listening
      />
    </template>

    <!-- 속성 추가 버튼 -->
    <v-text
      :config="addFieldConfig"
      style="cursor: pointer"
      @click="onAddField($event)"
      listening
    />

    <!-- Anchor 점 -->
    <template v-for="dir in ['top','right','bottom','left']" :key="dir">
      <v-circle :config="getAnchorConfig(dir)" />
      <v-rect
        :config="getAnchorHitConfig(dir)"
        @mousedown="onAnchorClick(dir, $event)"
        listening
      />
    </template>
  </v-group>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({ entity: Object, selected: Boolean })
const emit = defineEmits(['select','update','context','anchor-click','edit'])

const boxWidth = 180
const fieldHeight = 24
const anchorHitSize = 16
const isDragging = ref(false)

const computedHeight = computed(() => 40 + props.entity.fields.length * fieldHeight + 24)

const groupConfig = computed(() => ({
  x: props.entity.x,
  y: props.entity.y,
  draggable: true,
  dragBoundFunc: pos => isDragging.value ? pos : { x: props.entity.x, y: props.entity.y },
  id: props.entity.id
}))

const rectConfig = computed(() => ({
  width: boxWidth,
  height: computedHeight.value,
  fill: '#fff',
  stroke: props.selected ? '#3b82f6' : '#9ca3af',
  strokeWidth: 2,
  cornerRadius: 6
}))

const tableNameConfig = computed(() => ({
  text: props.entity.name,
  x: 10,
  y: 10,
  fontSize: 16,
  fontFamily: 'Arial',
  fill: '#111827',
  listening: true
}))

function getFieldConfig(i, field) {
  return {
    text: `${field.name}: ${field.type}`,
    x: 10,
    y: 40 + i * fieldHeight,
    fontSize: 14,
    fontFamily: 'Arial',
    fill: '#000',
    listening: true
  }
}

function getDeleteConfig(i) {
  return {
    text: '×',
    x: boxWidth - 14,
    y: 40 + i * fieldHeight,
    fontSize: 14,
    fontFamily: 'Arial',
    fill: '#dc2626',
    listening: true
  }
}

const addFieldConfig = computed(() => ({
  text: '＋ 속성 추가',
  x: 10,
  y: computedHeight.value - 18,
  fontSize: 13,
  fontFamily: 'Arial',
  fill: '#2563eb',
  listening: true
}))

function onMouseDown() { isDragging.value = true }
function onMouseUp()   { isDragging.value = false }
function onDragMove(e) {
  if (!isDragging.value) return
  emit('update', { ...props.entity, x: e.target.x(), y: e.target.y() })
}

function onClick(e) {
  // background click
  emit('select', props.entity)
}

function onContextMenu(e) {
  emit('context', { entity: props.entity, event: e.evt })
}

function onNameDblClick(e) {
  // Konva event: call e.evt.stopPropagation()
  e.evt.stopPropagation()
  emit('edit', { type: 'name', entity: props.entity, clientPos: { x: e.evt.clientX, y: e.evt.clientY } })
}

function onFieldDblClick(index, e) {
  e.evt.stopPropagation()
  emit('edit', { type: 'field', entity: props.entity, fieldIndex: index, clientPos: { x: e.evt.clientX, y: e.evt.clientY } })
}

function onRemoveField(index, e) {
  e.evt.stopPropagation()
  const fields = [...props.entity.fields]
  fields.splice(index,1)
  emit('update', { ...props.entity, fields })
}

function onAddField(e) {
  e.evt.stopPropagation()
  const newField = { id: crypto.randomUUID(), name: 'newID', type: 'int' }
  emit('update', { ...props.entity, fields: [...props.entity.fields, newField] })
}

function onAnchorClick(direction, e) {
  console.log('[ErdEntityBox] anchor clicked →', props.entity.id, direction)
  e.evt.stopPropagation()
  emit('anchor-click', { id: props.entity.id, direction })
  
}

function getAnchorConfig(direction) {
  const h = computedHeight.value
  const pos = {
    top:    { x: boxWidth/2, y: 0 },
    right:  { x: boxWidth,   y: h/2 },
    bottom: { x: boxWidth/2, y: h },
    left:   { x: 0,          y: h/2 }
  }
  return { x: pos[direction].x, y: pos[direction].y, radius:5, fill:'#1976d2', stroke:'#fff', strokeWidth:1 }
}

function getAnchorHitConfig(direction) {
  const { x, y } = getAnchorConfig(direction)
  return { x: x - anchorHitSize/2, y: y - anchorHitSize/2, width: anchorHitSize, height: anchorHitSize,fill: 'transparent', listening: true }
}
</script>

<style scoped>
/* 필요시 추가 스타일링 */
</style>
