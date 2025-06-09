<template>
<v-group
  :x="config.x"
  :y="config.y"
  :draggable="!isEditing && !midDragging"
  @dragend="emitPosition"
   @contextmenu="onBoxRightClick"
>


    <!-- 박스 배경 -->
    <v-rect
      :width="boxWidth"
      :height="totalHeight"
      fill="#ffffff"
      stroke="#999"
      :strokeWidth="1.2"
      :cornerRadius="2"
    />

    <!-- 헤더 -->
    <v-rect
      :width="boxWidth"
      :height="headerHeight"
      fill="#f2f2f2"
      stroke="#ccc"
      :strokeWidth="1"
    />
    <v-line
      :points="[0, headerHeight, boxWidth, headerHeight]"
      stroke="#ccc"
      :strokeWidth="1"
    />

    <!-- 테이블명 -->
    <v-text
      :text="config.name"
      :x="0"
      :y="8"
      :width="boxWidth"
      align="center"
      :fontSize="14"
      fontStyle="bold"
      fill="#222"
      @dblclick="onEditTableName"
    />

    <!-- 필드 라인 + 텍스트 -->
    <v-line
      v-for="(field, index) in config.fields"
      :key="'line-' + index"
      :points="[0, headerHeight + (index + 1) * fieldHeight, boxWidth, headerHeight + (index + 1) * fieldHeight]"
      stroke="#ddd"
      :strokeWidth="1"
    />
    <v-text
      v-for="(field, index) in config.fields"
      :key="'field-' + index"
      :text="formatField(field)"
      :x="10"
      :y="headerHeight + index * fieldHeight + 6"
      :fontSize="12"
      fill="#333"
      @dblclick="(e) => onEditField(e, index, field)"
    />
    <!-- + 필드 추가 버튼 -->
<v-text
  :x="10"
  :y="headerHeight + config.fields.length * fieldHeight + 6"
  text="＋ 필드 추가"
  :fontSize="12"
  fill="#1976d2"
  fontStyle="bold"
  @click="addField"
/>



    <!-- 앵커 포인트 (4방향) -->
    <v-circle
      v-for="dir in ['top', 'bottom', 'left', 'right']"
      :key="dir"
      :x="anchorX(dir)"
      :y="anchorY(dir)"
      :radius="5"
      fill="#ffffff"
      stroke="#007bff"
      :strokeWidth="1.5"
      @click="emitAnchorClick(dir)"
    />
  </v-group>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  config: Object,
  isEditing: Boolean, // ⬅️ 추가!
  midDragging: Boolean // 추가!
})

const emit = defineEmits(['update-position', 'anchor-click', 'edit-table', 'edit-field','add-field','open-context'])

const boxWidth = 180
const headerHeight = 30
const fieldHeight = 26
const totalHeight = computed(() => {
  return headerHeight + props.config.fields.length * fieldHeight + 30
})


const addField = () => {
  emit('add-field', props.config.id)
}



const emitPosition = (e) => {
  emit('update-position', {
    id: props.config.id,
    x: e.target.x(),
    y: e.target.y()
  })
}


const emitAnchorClick = (direction) => {
  emit('anchor-click', { boxId: props.config.id, direction })
}

const formatField = (field) => {
  const label = field.type === 'PK' ? 'PK' : field.type === 'FK' ? 'FK' : ''
  return `${label ? label + '  ' : ''}${field.name}`
}


const anchorX = (dir) => {
  if (dir === 'left') return 0
  if (dir === 'right') return boxWidth
  return boxWidth / 2
}

const anchorY = (dir) => {
  if (dir === 'top') return 0
  if (dir === 'bottom') return totalHeight.value        // ✅ .value 붙이기
  return totalHeight.value / 2                          // ✅ .value 붙이기
}

const onEditTableName = (e) => {
  if (e.evt) {
    e.evt.stopPropagation()
    emit('start-edit', {
      boxId: props.config.id,
      type: 'table',
      index: null,
      value: props.config.name,
      x: e.evt.clientX,
      y: e.evt.clientY
    })
  }
}

const onEditField = (e, index, field) => {
  if (e.evt) {
    e.evt.stopPropagation()
    emit('start-edit', {
      boxId: props.config.id,
      type: 'field',
      index,
      value: field.name,
      fieldType: field.type || '', // 🔥 현재 타입(FK/PK/없음) 같이 넘김
      x: e.evt.clientX,
      y: e.evt.clientY
    })
  }
}

function onBoxRightClick(e) {
  if (e.evt && typeof e.evt.preventDefault === 'function') e.evt.preventDefault();
  emit('open-context', { box: props.config, event: e.evt });
}

</script>

<style scoped>
/* 스타일은 필요 시 추가 */
</style>
