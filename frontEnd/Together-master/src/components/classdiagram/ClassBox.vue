<template>
  <v-group
    :config="groupConfig"
    @dragmove="handleDragMove"
    @click="handleClick"
  >
    <!-- 박스 배경 -->
    <v-rect :config="rectConfig" />

    <!-- 헤더 영역 -->
    <v-rect :config="headerRectConfig" />
    <v-text
      :config="classNameConfig"
      @dblclick="(e) => $emit('edit', { cls, region: 'name', index: null, event: e })"
      @contextmenu="(e) => { e.evt.preventDefault(); e.evt.cancelBubble = true; $emit('contextmenu', e, cls); }"
    />

    <!-- 속성 목록 -->
    <v-text
      v-for="(attr, i) in cls.attributes"
      :key="'attr-' + i"
      :config="getAttributeConfig(i, attr)"
      @click="() => emit('select-attribute', { clsId: cls.id, index: i })"
      @contextmenu="(e) => emit('rightclick-attribute', { clsId: cls.id, index: i, event: e })"
      @dblclick="(e) => $emit('edit', { cls, region: 'attributes', index: i, event: e })"
    />

    <!-- 속성 추가 버튼 -->
    <v-text
      :config="plusAttributeConfig"
      @mousedown="stopPropagation"
      @click="() => $emit('add-item', 'attributes', cls.id)"
    />

    <!-- 메서드 목록 -->
    <v-text
      v-for="(mth, j) in cls.methods"
      :key="'mth-' + j"
      :config="getMethodConfig(j, mth)"
      @click="() => emit('select-method', { clsId: cls.id, index: j })"
      @contextmenu="(e) => emit('rightclick-method', { clsId: cls.id, index: j, event: e })"
      @dblclick="(e) => $emit('edit', { cls, region: 'methods', index: j, event: e })"
    />

    <!-- 메서드 추가 버튼 -->
    <v-text
      :config="plusMethodConfig"
      @mousedown="stopPropagation"
      @click="() => $emit('add-item', 'methods', cls.id)"
    />

    <!-- 구분선 -->
    <v-line :config="getSeparatorY(separatorY)" />

    <!-- Anchor 포인트 -->
    <v-circle :config="getAnchorConfig('top')" @click="() => emit('anchor-click', { id: cls.id, direction: 'top' })" />
    <v-circle :config="getAnchorConfig('right')" @click="() => emit('anchor-click', { id: cls.id, direction: 'right' })" />
    <v-circle :config="getAnchorConfig('bottom')" @click="() => emit('anchor-click', { id: cls.id, direction: 'bottom' })" />
    <v-circle :config="getAnchorConfig('left')" @click="() => emit('anchor-click', { id: cls.id, direction: 'left' })" />
  </v-group>
</template>



<script setup>
import { computed, watch, onMounted, nextTick } from 'vue';
import { getTextWidth } from '@/utils/diagramUtils';

const props = defineProps({
  cls: Object,
  selected: Boolean,
  scale: Object,
  selectedMethod: Object, // ✅ 추가
  selectedAttribute: Object, // 추가
  editing: Boolean, // ✅ 현재 편집 중인지 여부
});


const emit = defineEmits([
  'dragmove',
  'edit',
  'add-item',
  'contextmenu',
  'box-selected',
  'anchor-click',
  'update-size'
]);

const headerHeight = 30;
const lineHeight = 20;
const btnHeight = 20;

const boxWidth = computed(() => {
  const nameWidth = getTextWidth(props.cls.name, '18px Arial') + 20;
  const attrWidth = Math.max(...props.cls.attributes.map(a => getTextWidth(a, '14px Arial')), 0) + 20;
  const mthWidth = Math.max(...props.cls.methods.map(m => getTextWidth(m, '14px Arial')), 0) + 20;
  return Math.max(200, nameWidth, attrWidth, mthWidth);
});

const totalHeight = computed(() => {
  return headerHeight +
    (props.cls.attributes.length ? props.cls.attributes.length * lineHeight : 0) +
    (props.cls.methods.length ? props.cls.methods.length * lineHeight : 0) +
    btnHeight + 32;
});

const separatorY = computed(() => {
  return headerHeight +
    (props.cls.attributes.length * lineHeight) +
    btnHeight + 5;
});

function notifySizeUpdate() {
  emit('update-size', {
    id: props.cls.id,
    width: boxWidth.value,
    height: totalHeight.value,
  });
}

onMounted(() => {
  nextTick(() => {
    notifySizeUpdate();
  });
});

watch([boxWidth, totalHeight], () => {
  notifySizeUpdate();
});

const groupConfig = computed(() => ({
  x: props.cls.x,
  y: props.cls.y,
  draggable: !props.editing, // ✅ 편집 중엔 드래그 비활성화
  id: props.cls.id,
}));

const rectConfig = computed(() => ({
  width: boxWidth.value,
  height: totalHeight.value,
  fill: '#ffffff',
  stroke: props.selected ? '#4cafef' : '#222',
  strokeWidth: 1.5,
  cornerRadius: 4
}));

const headerRectConfig = computed(() => ({
  x: 0,
  y: 0,
  width: boxWidth.value,
  height: headerHeight,
  fill: '#111',
  stroke: '#111',
  strokeWidth: 1
}));

const classNameConfig = computed(() => ({
  text: props.cls.name,
  x: 0,
  y: 5,
  width: boxWidth.value,
  fontSize: 16,
  fontFamily: 'Arial',
  fontStyle: 'italic',
  align: 'center',
  fill: '#fff',
}));

const plusAttributeConfig = computed(() => ({
  text: '+',
  x: (boxWidth.value - 20) / 2,
  y: headerHeight + props.cls.attributes.length * lineHeight + 5,
  fontSize: 14,
  fill: '#4caf50',
  width: 20,
  align: 'center',
  cursor: 'pointer'
}));

const plusMethodConfig = computed(() => ({
  text: '+',
  x: (boxWidth.value - 20) / 2,
  y: separatorY.value + props.cls.methods.length * lineHeight + 5,
  fontSize: 14,
  fill: '#4caf50',
  width: 20,
  align: 'center',
  cursor: 'pointer'
}));

const getAttributeConfig = (i, attr) => {
  return {
    text: attr,
    x: 5,
    y: headerHeight + 5 + i * lineHeight,
    fontSize: 14,
    width: boxWidth.value - 10,
    fill: isSelectedAttribute(i) ? '#f44336' : '#000',
    fontStyle: isSelectedAttribute(i) ? 'bold' : 'normal',
    align: 'left',
    name: `attr-${i}`,
    listening: true, // 🔥 이거 없으면 click/ctxmenu 안 먹힘
  };
};


function isSelectedAttribute(index) {
  return props.selectedAttribute?.clsId === props.cls.id && props.selectedAttribute?.index === index;
}


const getMethodConfig = (j, mth) => {
  return {
    text: mth,
    x: 5,
    y: separatorY.value + 5 + j * lineHeight,
    fontSize: 14,
    width: boxWidth.value - 10,
    fill: isSelectedMethod(j) ? '#f44336' : '#000',
    fontStyle: isSelectedMethod(j) ? 'bold' : 'normal',
    align: 'left',
    name: `method-${j}`,
    padding: 4,
    listening: true, // ✅ 이걸 빼먹으면 안됨
  };
};


function isSelectedMethod(index) {
  return props.selectedMethod?.clsId === props.cls.id && props.selectedMethod?.index === index;
}


const getSeparatorY = (y) => {
  const isValid = typeof y === 'number' && !isNaN(y);
  return {
    points: isValid ? [0, y, boxWidth.value || 200, y] : [],
    stroke: '#333',
    strokeWidth: 2,
    strokeOpacity: 1,
    visible: isValid,
  };
};

function getAnchorConfig(direction) {
  const pos = {
    top: { x: boxWidth.value / 2, y: 0 },
    right: { x: boxWidth.value, y: totalHeight.value / 2 },
    bottom: { x: boxWidth.value / 2, y: totalHeight.value },
    left: { x: 0, y: totalHeight.value / 2 },
  };
  return {
    x: pos[direction].x,
    y: pos[direction].y,
    radius: 4,
    fill: '#1976d2',
    stroke: '#fff',
    strokeWidth: 1,
    listening: true
  };
}

function handleDragMove(e) {
  emit('dragmove', {
    id: props.cls.id,
    x: e.target.x(),
    y: e.target.y(),
  });
}

function handleClick() {
  emit('box-selected', props.cls);
}

function stopPropagation(e) {
  e.evt.stopPropagation();
  e.evt.cancelBubble = true;
}
</script>

<style scoped>
</style>