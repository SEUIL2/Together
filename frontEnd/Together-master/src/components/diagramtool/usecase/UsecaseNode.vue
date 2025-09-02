<template>
  <v-group
    :config="{ x: config.x, y: config.y, draggable: !anchorHovering }"
    @dragend="handleDragEnd"
    @contextmenu="handleContextMenu"
  >
    <!-- 유스케이스 타원 (ellipse) -->
    <v-ellipse
      :config="{
        x: 0,
        y: 0,
        width: 150,
        height: 56,
        fill: '#fff',
        stroke: '#1976d2',
        strokeWidth: 2.5,
        shadowColor: '#1976d233',
        shadowBlur: 9,
        shadowOffset: { x: 2, y: 2 },
        shadowOpacity: 0.3
      }"
    />

    <!-- Anchor 점 (상/하/좌/우) -->
    <v-circle
      v-for="dir in anchorDirs"
      :key="dir"
      :x="anchorPos[dir].x"
      :y="anchorPos[dir].y"
      :radius="6"
      :fill="anchorActive ? (hoveredAnchor === dir ? '#1976d2' : '#5ea3f7') : '#e0e4ed'"
      :opacity="anchorActive || hoveredAnchor === dir ? 0.88 : 0.25"
      :stroke="hoveredAnchor === dir ? '#1976d2' : '#888'"
      :strokeWidth="2"
      @mouseover="handleAnchorMouseOver(dir)"
      @mouseout="handleAnchorMouseOut"
      @click="handleAnchorClick(dir, $event)"
      style="pointer-events: auto;"
    />

    <!-- 이름 텍스트 -->
    <v-text
      :config="{
        text: config.name,
        x: -70,
        y: -12,
        width: 140,
        align: 'center',
        fontSize: 18,
        fontStyle: 'bold',
        fill: '#222',
        listening: true
      }"
      @dblclick="handleDblClick"
    />

    <!-- 이름 인풋 (수정 모드) -->
    <foreignObject v-if="editingLocal" :x="-70" :y="-14" :width="140" :height="32">
      <input
        class="usecase-edit-input"
        v-model="inputValue"
        @blur="handleFinishEdit"
        @keydown.enter="handleFinishEdit"
        ref="inputRef"
        style="width:138px;height:28px;font-size:17px;text-align:center;border-radius:6px;"
      />
    </foreignObject>
  </v-group>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const anchorHovering = ref(false)

const props = defineProps({
  config: { type: Object, required: true }, // { id, name, x, y }
  anchorActive: { type: Boolean, default: false },
  editing: { type: Boolean, default: false }
})
const emit = defineEmits([
  'update-position',
  'contextmenu',
  'anchor-click',
  'finish-edit'
])

// Anchor 점 위치
const anchorDirs = ['top', 'bottom', 'left', 'right']
const anchorPos = {
  top:    { x: 0,   y: -28 },
  bottom: { x: 0,   y: 28 },
  left:   { x: -75, y: 0 },
  right:  { x: 75,  y: 0 }
}
const hoveredAnchor = ref(null)

// --- Anchor 핸들러 ---
function handleAnchorMouseOver(dir) {
  anchorHovering.value = true
  hoveredAnchor.value = dir
}
function handleAnchorMouseOut() {
  anchorHovering.value = false
  hoveredAnchor.value = null
}
function handleAnchorClick(dir, e) {
  if (e && e.evt && typeof e.evt.stopPropagation === 'function') {
    e.evt.stopPropagation();
  }
  emit('anchor-click', { nodeId: props.config.id, direction: dir, type: 'usecase' })
}

// --- 노드 이동/컨텍스트 메뉴 ---
function handleDragEnd(e) {
  emit('update-position', props.config.id, e.target.x(), e.target.y())
}
function handleContextMenu(e) {
  emit('contextmenu', props.config.id, e)
}

// --- 더블클릭 수정 ---
const editingLocal = ref(false)
const inputValue = ref(props.config.name)
const inputRef = ref(null)

function openEdit() {
  editingLocal.value = true
  inputValue.value = props.config.name
  nextTick(() => { inputRef.value && inputRef.value.focus() })
}
const handleDblClick = (e) => {
  console.log('✅ [USECASE] 더블클릭 발생!', e)
  editingLocal.value = true
  inputValue.value = props.config.name
  nextTick(() => {
    inputRef.value && inputRef.value.focus()
  })
}


// 외부 editing prop 감지 (컨텍스트 메뉴 등)
watch(() => props.editing, (val) => {
  if (val) openEdit()
  else editingLocal.value = false
})
// name 동기화
watch(() => props.config.name, (val) => {
  if (!editingLocal.value) inputValue.value = val
})
// 인풋 종료
function handleFinishEdit() {
  editingLocal.value = false
  if (inputValue.value.trim() && inputValue.value !== props.config.name) {
    props.config.name = inputValue.value
  }
  emit('finish-edit')
}
</script>

<style scoped>
.usecase-edit-input {
  font-size: 17px;
  border: 1.4px solid #1976d2;
  padding: 2.5px 7px;
  border-radius: 6px;
  outline: none;
}
.usecase-edit-input:focus {
  border-color: #40a7ff;
  background: #e3f2fd;
}
</style>
