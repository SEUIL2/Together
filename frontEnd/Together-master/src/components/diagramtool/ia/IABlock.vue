<template>
  <v-group ref="groupRef"
    :x="config.x"
    :y="config.y"
    :draggable="!isEditing && !midDragging"
    @dragend="emitPosition"
@contextmenu="(e) => {
    if (e.evt) e.evt.preventDefault()
    emit('open-context', { 
      block: props.config, 
      event: e.evt
    })
  }"
  @mouseenter="isHovered = true"
  @mouseleave="isHovered = false"
  >
    <!-- 박스 배경 -->
    <v-rect
      :width="boxWidth"
      :height="totalHeight"
      fill="#ffffff"
      stroke="#999"
      :strokeWidth="1.2"
      :cornerRadius="4"
      :shadowColor="'#000'"
      :shadowBlur="10"
      :shadowOpacity="0.1"
      :shadowOffsetX="0"
      :shadowOffsetY="4"
    />

    <!-- 블록 제목 영역 -->
    <v-rect
      :width="boxWidth"
      :height="headerHeight"
      fill="#f5f5f5"
      stroke="#ccc"
    />
    <v-line
      :points="[0, headerHeight, boxWidth, headerHeight]"
      stroke="#ccc"
    />
    <v-text
      :text="config.title"
      :x="0"
      :y="8"
      :width="boxWidth"
      align="center"
      :fontSize="14"
      fontStyle="bold"
      fill="#333"
      @dblclick="onEditTitle"
    />

    <!-- 항목 라인 + 텍스트 -->
    <v-line
      v-for="(item, index) in config.items"
      :key="'line-' + index"
      :points="[0, headerHeight + (index + 1) * itemHeight, boxWidth, headerHeight + (index + 1) * itemHeight]"
      stroke="#eee"
    />
    <v-text
      v-for="(item, index) in config.items"
      :key="'item-' + index"
      :text="item.title"
      :x="10"
      :y="headerHeight + index * itemHeight + 6"
      :fontSize="12"
      fill="#333"
      @dblclick="(e) => onEditItem(e, index, item)"
    />

    <!-- + 항목 추가 -->
    <v-text
      :x="10"
      :y="headerHeight + config.items.length * itemHeight + 6"
      text="＋ 항목 추가"
      :fontSize="12"
      fill="#1976d2"
      fontStyle="bold"
      @click="addItem"
    />

    <!-- 네 방향 앵커 -->
    <v-circle
      v-for="dir in ['top', 'bottom', 'left', 'right']"
      :key="dir"
      :x="anchorX(dir)"
      :y="anchorY(dir)"
      :visible="isHovered"
      :radius="5"
      fill="#fff"
      stroke="#007bff"
      :strokeWidth="1.5"
      @click="() => emitAnchorClick(dir)"
    />
    <!-- 각 항목 오른쪽 앵커 -->
    <v-circle
      v-for="(item, index) in config.items"
      :key="'item-anchor-' + index"
      :x="boxWidth - 12"
      :y="headerHeight + index * itemHeight + itemHeight / 2"
      :visible="isHovered"
      :radius="4"
      fill="#fff"
      stroke="#000"
      :strokeWidth="1.5"
      @click="() => emitItemAnchorClick(index)"
    />
  </v-group>
</template>

<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  config: Object,
  isEditing: Boolean,
  midDragging: Boolean
})
const emit = defineEmits([
  'update',         // 위치 갱신
  'anchor-click',   // 관계선 앵커 클릭
  'edit-title',     // 제목 더블클릭
  'edit-item',      // 항목 더블클릭
  'add-item',       // 항목 추가 클릭
  'open-context'    // 우클릭 메뉴 (박스)
])

const isHovered = ref(false)

const boxWidth = 180
const headerHeight = 30
const itemHeight = 26
const totalHeight = computed(() =>
  headerHeight + props.config.items.length * itemHeight + 30
)

function emitPosition(e) {
  emit('update', {
    ...props.config,
    x: e.target.x(),
    y: e.target.y()
  })
}

function emitAnchorClick(dir) {
  const localX = anchorX(dir)
  const localY = anchorY(dir)
  const absoluteX = props.config.x + localX
  const absoluteY = props.config.y + localY

  emit('anchor-click', {
    boxId: props.config.id,
    direction: dir,
    pos: [absoluteX, absoluteY]
  })
}

const emitItemAnchorClick = (index) => {
  const localX = boxWidth - 12
  const localY = headerHeight + index * itemHeight + itemHeight / 2
  const absoluteX = props.config.x + localX
  const absoluteY = props.config.y + localY

  emit('anchor-click', {
    boxId: props.config.id,
    direction: 'item',
    itemIndex: index,
    pos: [absoluteX, absoluteY]
  })
}

const anchorX = (dir) => {
  if (dir === 'left') return 0
  if (dir === 'right') return boxWidth
  return boxWidth / 2
}
const anchorY = (dir) => {
  if (dir === 'top') return 0
  if (dir === 'bottom') return totalHeight.value
  if (dir === 'left' || dir === 'right') return totalHeight.value / 2
  return totalHeight.value / 2
}

function onEditTitle(e) {
  if (e.evt) {
    e.evt.stopPropagation()
    emit('edit-title', {
      id: props.config.id,
      value: props.config.title,
      x: e.evt.clientX,
      y: e.evt.clientY
    })
  }
}

function onEditItem(e, index, item) {
  if (e.evt) {
    e.evt.stopPropagation()
    emit('edit-item', {
      id: props.config.id,
      index,
      value: item,
      x: e.evt.clientX,
      y: e.evt.clientY
    })
  }
}
const addItem = () => emit('add-item', props.config.id)
</script>
