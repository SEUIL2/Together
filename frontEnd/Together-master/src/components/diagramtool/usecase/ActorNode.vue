<template>
  <v-group
    :config="{ x: config.x, y: config.y, draggable: !anchorHovering }"
    @dragend="onDragEnd"
    @contextmenu="onRightClick"
  >
    <!-- 사람 아이콘 (막대인형) -->
    <v-circle
      :config="{
        x: 0,
        y: 0,
        radius: 28,
        fill: '#fff',
        stroke: '#555',
        strokeWidth: 2,
        shadowColor: '#8884',
        shadowBlur: 6,
      }"
    />
    <!-- 몸통 -->
    <v-line :config="{ points: [0, 28, 0, 80], stroke: '#555', strokeWidth: 4 }" />
    <!-- 팔 -->
    <v-line :config="{ points: [-28, 48, 0, 48, 28, 48], stroke: '#555', strokeWidth: 4 }" />
    <!-- 다리 -->
    <v-line :config="{ points: [0, 80, -18, 110], stroke: '#555', strokeWidth: 4 }" />
    <v-line :config="{ points: [0, 80, 18, 110], stroke: '#555', strokeWidth: 4 }" />

    <!-- Anchor 점(상/하/좌/우) -->
    <v-circle
      v-for="dir in anchorDirs"
      :key="dir"
      :x="anchorPos[dir].x"
      :y="anchorPos[dir].y"
      :radius="6"
      :fill="anchorActive ? (hoveredAnchor === dir ? '#1976d2' : '#5ea3f7') : '#e0e4ed'"
      :opacity="anchorActive || hoveredAnchor === dir ? 0.85 : 0.3"
      :stroke="hoveredAnchor === dir ? '#1976d2' : '#888'"
      :strokeWidth="2"
      @mouseover="anchorHovering = true; hoveredAnchor = dir"
      @mouseout="anchorHovering = false; hoveredAnchor = null"
      @click="onAnchorClick(dir, $event)"
      style="pointer-events: auto;"
    />

    <!-- 이름 텍스트 -->
    <v-text
      :config="{
        text: config.name,
        x: -40,
        y: 120,
        width: 80,
        align: 'center',
        fontSize: 16,
        fontStyle: 'bold',
        fill: '#222',
        listening: true
      }"
      @dblclick="triggerEdit"
    />

    <!-- 이름 인풋 (수정 모드) -->
    <foreignObject v-if="editingLocal" :x="-40" :y="120" :width="80" :height="28">
      <input
        class="actor-edit-input"
        v-model="inputValue"
        @blur="finishEdit"
        @keydown.enter="finishEdit"
        ref="inputRef"
        style="width: 78px; height: 24px; font-size: 15px; text-align: center; border-radius: 4px;"
      />
    </foreignObject>
  </v-group>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'

const anchorHovering = ref(false)

const props = defineProps({
  config: { type: Object, required: true }, // { id, name, x, y }
  anchorActive: { type: Boolean, default: false }, // 연결모드 활성화(외부에서)
  editing: { type: Boolean, default: false }, // 부모에서 내려받는 수정모드
})
const emit = defineEmits([
  'update-position',    // (id, x, y)
  'contextmenu',        // (id, e)
  'anchor-click',       // ({ nodeId, direction, type })
  'finish-edit'
])

// Anchor 점 방향 및 상대좌표
const anchorDirs = ['top', 'bottom', 'left', 'right']
const anchorPos = {
  top:    { x: 0,    y: 0 },
  bottom: { x: 0,    y: 110 },
  left:   { x: -28,  y: 48 },
  right:  { x: 28,   y: 48 }
}

// Anchor UX
const hoveredAnchor = ref(null)

// Anchor 클릭 이벤트(연결 시작/종료점 지정)
const onAnchorClick = (dir, e) => {
  if (e && e.evt && typeof e.evt.stopPropagation === 'function') {
    e.evt.stopPropagation();
  }
  emit('anchor-click', { nodeId: props.config.id, direction: dir, type: 'actor' })
}

// 드래그 이동
const onDragEnd = (e) => {
  emit('update-position', props.config.id, e.target.x(), e.target.y())
}

// 우클릭(컨텍스트 메뉴)
const onRightClick = (e) => {
  emit('contextmenu', props.config.id, e)
}

// ==== 이름 더블클릭 수정 + 부모 수정모드 진입 모두 지원 ====
// 인풋 상태: 항상 내부 editingLocal만 체크!
const editingLocal = ref(false)
const inputValue = ref(props.config.name)
const inputRef = ref(null)

// 부모에서 editing prop이 true로 바뀌면 인풋 표시
watch(() => props.editing, (val) => {
  if (val) {
    openEdit()
  } else {
    editingLocal.value = false
  }
})

// config.name이 바뀔 때 inputValue 동기화
watch(() => props.config.name, (val) => {
  if (!editingLocal.value) inputValue.value = val
})

// 더블클릭: 직접 수정 진입
function triggerEdit() {
  openEdit()
}

// 수정모드 진입
function openEdit() {
  editingLocal.value = true
  inputValue.value = props.config.name
  nextTick(() => {
    inputRef.value && inputRef.value.focus()
  })
}

// 수정 종료
function finishEdit() {
  editingLocal.value = false
  if (inputValue.value.trim() && inputValue.value !== props.config.name) {
    props.config.name = inputValue.value
  }
  emit('finish-edit')
}
</script>

<style scoped>
.actor-edit-input {
  font-size: 15px;
  border: 1px solid #bbb;
  padding: 2px 6px;
  border-radius: 4px;
  outline: none;
}
.actor-edit-input:focus {
  border-color: #1976d2;
  background: #e3f2fd;
}
</style>
