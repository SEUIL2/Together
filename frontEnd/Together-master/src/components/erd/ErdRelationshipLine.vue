<template>
  <template v-if="fromPos && toPos">
    <!-- 꺾인 선(polyline) -->
    <v-line
      :config="lineConfig"
      @click="onClick"
      @contextmenu="onContextMenu"
    />

    <!-- 카디널리티 레이블 -->
    <v-text :config="fromLabelConfig" />
    <v-text :config="toLabelConfig" />

    <!-- 우클릭 메뉴 -->
    <foreignObject
      v-if="menuVisible"
      :x="menuPos.x"
      :y="menuPos.y"
      width="160"
      height="100"
    >
      <div class="context-menu" @click.stop>
        <label>
          From
          <select v-model="local.fromCardinality" @change="emitUpdate">
            <option value="1">| | (1)</option>
            <option value="0..1">O | (0..1)</option>
            <option value="1..*">| &lt; (1..*)</option>
            <option value="0..*">O &lt; (0..*)</option>
          </select>
        </label>
        <label>
          To
          <select v-model="local.toCardinality" @change="emitUpdate">
            <option value="1">| | (1)</option>
            <option value="0..1">O | (0..1)</option>
            <option value="1..*">| &lt; (1..*)</option>
            <option value="0..*">O &lt; (0..*)</option>
          </select>
        </label>
        <button class="delete-btn" @click="emitDelete">삭제</button>
      </div>
    </foreignObject>
  </template>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  rel:      { type: Object, required: true },
  entities: { type: Array,  required: true }
})
const emit = defineEmits(['update','delete'])

// 로컬 복사 & props 변경 감지
const local = ref({ ...props.rel })
watch(() => props.rel, v => { local.value = { ...v } })

// 상태
const selected     = ref(false)
const menuVisible  = ref(false)
const menuPos      = ref({ x:0, y:0 })

// 상수 (ErdEntityBox와 동일)
const boxWidth    = 180
const fieldHeight = 24

// Anchor 위치 계산
function getAnchorPos(entity, dir) {
  const height = 40 + entity.fields.length * fieldHeight + 24
  switch(dir) {
    case 'top':    return { x: entity.x + boxWidth/2, y: entity.y }
    case 'right':  return { x: entity.x + boxWidth,   y: entity.y + height/2 }
    case 'bottom': return { x: entity.x + boxWidth/2, y: entity.y + height }
    case 'left':   return { x: entity.x,               y: entity.y + height/2 }
  }
}

// from/to 좌표
const fromPos = computed(() => {
  const e = props.entities.find(e => e.id === local.value.fromEntityId)
  return e ? getAnchorPos(e, local.value.fromDirection) : null
})
const toPos = computed(() => {
  const e = props.entities.find(e => e.id === local.value.toEntityId)
  return e ? getAnchorPos(e, local.value.toDirection) : null
})

// 꺾인 선 포인트
const linePoints = computed(() => {
  const f = fromPos.value, t = toPos.value
  const midX = (f.x + t.x) / 2
  return [ f.x, f.y, midX, f.y, midX, t.y, t.x, t.y ]
})

// 선택 강조 색
const strokeColor = computed(() => selected.value ? '#ef4444' : '#4b5563')

// Vue-Konva용 config
const lineConfig = computed(() => ({
  id:          props.rel.id,
  points:      linePoints.value,
  stroke:      strokeColor.value,
  strokeWidth: 2,
  listening:   true
}))

// 레이블 설정
function labelConfig(pos, text) {
  return {
    text,
    x:          pos.x + (pos.x < (toPos.value?.x||0) ? -20 : 10),
    y:          pos.y + (pos.y < (toPos.value?.y||0) ? -10 : 10),
    fontSize:   12,
    fontFamily: 'Arial',
    fill:       '#111827'
  }
}
function getLabel(v) {
  switch(v) {
    case '1':    return '| |'
    case '0..1': return 'O |'
    case '1..*': return '| <'
    case '0..*': return 'O <'
    default:     return '?'
  }
}
const fromLabelConfig = computed(() =>
  local.value.fromCardinality
    ? labelConfig(fromPos.value, getLabel(local.value.fromCardinality))
    : {}
)
const toLabelConfig = computed(() =>
  local.value.toCardinality
    ? labelConfig(toPos.value, getLabel(local.value.toCardinality))
    : {}
)

// 이벤트 핸들러
function onClick(e) {
  e.evt.stopPropagation()
  selected.value    = !selected.value
  menuVisible.value = false
}
function onContextMenu(e) {
  e.evt.preventDefault()
  selected.value    = true
  menuPos.value     = { x: e.evt.offsetX + 10, y: e.evt.offsetY + 10 }
  menuVisible.value = true
}
function emitUpdate() {
  emit('update', { ...local.value })
}
function emitDelete() {
  emit('delete', props.rel.id)
  menuVisible.value = false
}
</script>

<style scoped>
.context-menu {
  background: white;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 8px;
  font-size: 12px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
}
.context-menu label {
  display: block;
  margin-bottom: 6px;
}
.context-menu select {
  width: 100%;
  font-size: 12px;
  margin-top: 2px;
}
.delete-btn {
  width: 100%;
  background: #ef4444;
  color: white;
  border: none;
  padding: 4px 0;
  font-size: 12px;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 6px;
}
</style>
