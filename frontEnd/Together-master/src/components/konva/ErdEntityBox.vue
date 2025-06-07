<template>
  <v-group :x="config.x" :y="config.y" draggable @dragend="emitPosition">
    <!-- 배경 박스 -->
    <v-rect :width="boxWidth" :height="totalHeight" fill="white" stroke="#333" cornerRadius="8" shadowBlur="4" />

    <!-- 테이블명 -->
    <v-rect :width="boxWidth" :height="headerHeight" fill="#1976d2" />
    <v-text
      :text="config.name"
      :x="12"
      :y="8"
      fontSize="15"
      fill="white"
      fontStyle="bold"
      @dblclick="editingName = true"
    />
    <v-html v-if="editingName" :x="12" :y="6">
      <input
        class="name-edit-input"
        v-model="config.name"
        @blur="editingName = false"
        style="font-size:14px; width:100px"
      />
    </v-html>

    <!-- 필드 목록 -->
    <v-group v-for="(field, index) in config.fields" :key="index" :y="headerHeight + index * fieldHeight">
      <v-text
        :text="formatField(field)"
        :x="12"
        :y="8"
        fontSize="13"
        fill="#333"
        @dblclick="editField(index)"
      />
      <v-html v-if="field.editing" :x="12" :y="6">
        <input
          v-model="field.name"
          @blur="field.editing = false"
          style="width: 120px; font-size: 13px"
        />
        <select v-model="field.type" style="margin-left: 5px; font-size: 13px;">
          <option value="PK">PK</option>
          <option value="FK">FK</option>
          <option value="NORMAL">일반</option>
        </select>
      </v-html>
    </v-group>

    <!-- Anchor 포인트 -->
    <v-circle
      v-for="dir in ['top', 'bottom', 'left', 'right']"
      :key="dir"
      :x="anchorX(dir)"
      :y="anchorY(dir)"
      :radius="6"
      fill="#1976d2"
      @click="emitAnchorClick(dir)"
    />
  </v-group>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({ config: Object });
const emit = defineEmits(['update-position', 'anchor-click']);

const boxWidth = 180;
const headerHeight = 30;
const fieldHeight = 28;

const editingName = ref(false);

const emitPosition = (e) => {
  const node = e.target;
  if (node && typeof node.x === 'function' && typeof node.y === 'function') {
    emit('update-position', {
      id: props.config.id,
      x: node.x(),
      y: node.y()
    });
  }
};


const emitAnchorClick = (direction) => {
  emit('anchor-click', { boxId: props.config.id, direction });
};

const formatField = (field) => {
  return `[${field.type}] ${field.name}`;
};

const anchorX = (dir) => {
  if (dir === 'left') return 0;
  if (dir === 'right') return boxWidth;
  if (dir === 'top' || dir === 'bottom') return boxWidth / 2;
};

const anchorY = (dir) => {
  const totalHeight = headerHeight + props.config.fields.length * fieldHeight;
  if (dir === 'top') return 0;
  if (dir === 'bottom') return totalHeight;
  if (dir === 'left' || dir === 'right') return totalHeight / 2;
};

const editField = (index) => {
  props.config.fields[index].editing = true;
};
</script>
