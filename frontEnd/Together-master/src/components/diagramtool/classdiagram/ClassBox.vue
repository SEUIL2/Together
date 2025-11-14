<template>
  <v-group :config="groupConfig" @dragend="onDragEnd" @dragmove="onDragMove" @contextmenu="onContextMenu" @mouseenter="isHovered = true" @mouseleave="isHovered = false">
    <!-- 박스 배경 -->
    <!-- Caching to improve performance -->
    <v-rect :config="boxConfig" ref="boxRef" />

    <!-- 클래스명 -->
    <v-text :config="nameConfig" @dblclick="startEditing('name', -1, $event)" />

    <!-- 속성 목록 -->
    <v-group v-for="(attr, i) in config.attributes" :key="`attr-group-${i}`">
      <v-text :config="getAttributeConfig(i, attr)" @dblclick="startEditing('attribute', i, $event)" />
      <v-text :config="getDeleteButtonConfig(getAttributeConfig(i, attr).y)" @click="deleteAttribute(i)" />
    </v-group>

    <!-- 구분선 (속성+ 추가 버튼 아래 18px) -->
    <v-line :config="separator1Config" />
    <v-line :config="separator2Config" />

    <!-- 메서드 목록 -->
    <v-group v-for="(method, i) in config.methods" :key="`method-group-${i}`">
      <v-text :config="getMethodConfig(i, method)" @dblclick="startEditing('method', i, $event)" />
      <v-text :config="getDeleteButtonConfig(getMethodConfig(i, method).y)" @click="deleteMethod(i)" />
    </v-group>

    <!-- 속성 추가 버튼 -->
    <v-group :config="attrAddBtnGroupConfig" @click="addAttribute">
      <v-rect :config="addBtnRectConfig" />
      <v-text :config="addBtnTextConfig" />
    </v-group>

    <!-- 메서드 추가 버튼 -->
    <v-group :config="methodAddBtnGroupConfig" @click="addMethod">
      <v-rect :config="addBtnRectConfig" />
      <v-text :config="addBtnTextConfig" />
    </v-group>

    <!-- Anchor 포인트 (상/하/좌/우) -->
    <v-circle v-for="anchor in anchors" :key="anchor.id" :config="anchor"
      @mouseenter="e => e.target.getStage().container().style.cursor = 'crosshair'"
      @mouseleave="e => e.target.getStage().container().style.cursor = 'default'" @click="onAnchorClick" />
  </v-group>
</template>

<script setup>
import { computed, nextTick, ref, onMounted, watch } from 'vue';
import Konva from 'konva';
import { debounce } from 'lodash';

const props = defineProps({ config: Object });
const emit = defineEmits([
  'update-position', 'anchor-click', 'contextmenu', 'height-update', 'width-update',
  'update-attribute', 'delete-attribute', 'update-method', 'delete-method', 'update-name'
]);

// --- Layout Constants ---
const headerHeight = 44;
const itemLineHeight = 22;
const addBtnGap = 8;
const addBtnHeight = 18;
const methodSectionGap = 10;
const sectionVPadding = 12;
const minBoxHeight = 80;
const itemHPadding = 12;
const deleteBtnWidth = 20;

const boxRef = ref(null);
const isHovered = ref(false);
const textHeightCache = ref({});

// Konva Text 노드의 실제 높이를 계산하는 함수
const measureTextNodeHeight = (text, width, fontSize) => {
  if (!text) return itemLineHeight;
  const konvaText = new Konva.Text({ text, width, fontSize, padding: 2 });
  return konvaText.height();
};
// Konva Text 노드의 실제 너비를 계산하는 함수 (한 줄 기준)
const measureTextNodeWidth = (text, fontSize) => {
  if (!text) return 0;
  const konvaText = new Konva.Text({ text, fontSize, padding: 2 });
  return konvaText.width();
};

onMounted(() => {
  // 초기 렌더링 시 높이 계산
  props.config.attributes.forEach((attr, i) => {
    textHeightCache.value[`attr-${i}`] = measureTextNodeHeight(attr, props.config.width - (itemHPadding * 2) - deleteBtnWidth, 14);
  });
  props.config.methods.forEach((method, i) => {
    textHeightCache.value[`method-${i}`] = measureTextNodeHeight(method, props.config.width - (itemHPadding * 2) - deleteBtnWidth, 14);
  });
});

// --- Computed properties for dynamic layout ---
const attributesStartY = headerHeight + sectionVPadding;
const attributesHeight = computed(() => props.config.attributes.reduce((sum, _, i) => sum + (textHeightCache.value[`attr-${i}`] || itemLineHeight), 0));
const attrAddBtnY = computed(() => attributesStartY + attributesHeight.value + addBtnGap);
const attrSectionBottom = computed(() => attrAddBtnY.value + addBtnHeight);
const methodsStartY = computed(() => attrSectionBottom.value + methodSectionGap);
const methodsHeight = computed(() => props.config.methods.reduce((sum, _, i) => sum + (textHeightCache.value[`method-${i}`] || itemLineHeight), 0));
const methodAddBtnY = computed(() => methodsStartY.value + methodsHeight.value);
const boxHeight = computed(() => Math.max(methodAddBtnY.value + addBtnHeight + sectionVPadding, minBoxHeight));

// --- 자동 너비 조절 로직 ---
const requiredWidth = computed(() => {
  const allTexts = [props.config.name, ...props.config.attributes, ...props.config.methods];
  const padding = itemHPadding * 2 + deleteBtnWidth; // 좌우 여백 + 삭제 버튼 너비
  const namePadding = 40; // 이름은 폰트가 크므로 여유 공간 추가

  const maxTextWidth = allTexts.reduce((maxWidth, text) => {
    const isName = text === props.config.name;
    const textWidth = measureTextNodeWidth(text, isName ? 16 : 14); // 텍스트의 실제 너비 측정
    return Math.max(maxWidth, textWidth + (isName ? namePadding : padding) + 5);
  }, 0); // 너비 계산의 기준을 0부터 시작하도록 변경
  return Math.max(160, maxTextWidth); // 최소 너비 160px 보장
});

// 박스 높이가 변경될 때마다 부모에게 알림
const debouncedHeightUpdate = debounce((newHeight) => {
  emit('height-update', { id: props.config.id, height: newHeight });
}, 100);
watch(boxHeight, debouncedHeightUpdate);

// 박스 너비가 변경될 때마다 부모에게 알림
const debouncedWidthUpdate = debounce((newWidth) => {
  if (Math.abs(newWidth - props.config.width) > 1) { // 너비가 달라졌을 때만 업데이트 (미세한 차이 무시)
    emit('width-update', { id: props.config.id, width: newWidth });
  }
}, 100);
watch(requiredWidth, debouncedWidthUpdate, { immediate: true });

// --- Konva Node Configurations ---
const groupConfig = computed(() => ({
  id: props.config.id, x: props.config.x, y: props.config.y, draggable: true
}));

const boxConfig = computed(() => ({
  width: props.config.width, height: boxHeight.value, fill: '#fff', stroke: '#4A5568',
  strokeWidth: 1.5, cornerRadius: 8, shadowColor: 'black', shadowBlur: 8,
  shadowOpacity: 0.1, shadowOffsetX: 0, shadowOffsetY: 4
}));

const nameConfig = computed(() => ({
  text: props.config.name, x: 0, y: 0, width: props.config.width, height: headerHeight,
  align: 'center', verticalAlign: 'middle', fontStyle: 'bold', fontSize: 16, fill: '#2D3748'
}));

const separator1Config = computed(() => ({
  points: [0, headerHeight, props.config.width, headerHeight], stroke: '#CBD5E0', strokeWidth: 1
}));

const separator2Config = computed(() => ({
  points: [0, attrSectionBottom.value, props.config.width, attrSectionBottom.value], stroke: '#CBD5E0', strokeWidth: 1
}));

const getAttributeConfig = (i, text) => {
  const yPos = attributesStartY + props.config.attributes.slice(0, i).reduce((sum, _, j) => sum + (textHeightCache.value[`attr-${j}`] || itemLineHeight), 0);
  return {
    text, x: itemHPadding, y: yPos, fontSize: 14, fill: '#4A5568',
    width: props.config.width - (itemHPadding * 2) - deleteBtnWidth, padding: 2,
    height: textHeightCache.value[`attr-${i}`] || itemLineHeight,
    verticalAlign: 'top'
  };
};

const getMethodConfig = (i, text) => {
  const yPos = methodsStartY.value + props.config.methods.slice(0, i).reduce((sum, _, j) => sum + (textHeightCache.value[`method-${j}`] || itemLineHeight), 0);
  return {
    text, x: itemHPadding, y: yPos, fontSize: 14, fill: '#4A5568',
    width: props.config.width - (itemHPadding * 2) - deleteBtnWidth, padding: 2,
    height: textHeightCache.value[`method-${i}`] || itemLineHeight,
    verticalAlign: 'top'
  };
};

const getDeleteButtonConfig = (y) => ({
  text: '×', x: props.config.width - itemHPadding - deleteBtnWidth, y: y,
  width: deleteBtnWidth, fontSize: 18, fill: '#A0AEC0', align: 'center', padding: 2,
  cursor: 'pointer'
});

const attrAddBtnGroupConfig = computed(() => ({ x: itemHPadding, y: attrAddBtnY.value }));
const methodAddBtnGroupConfig = computed(() => ({ x: itemHPadding, y: methodAddBtnY.value }));

const addBtnRectConfig = computed(() => ({
  width: props.config.width - (itemHPadding * 2), height: addBtnHeight, fill: '#EDF2F7', cornerRadius: 4
}));

const addBtnTextConfig = {
  text: '+', x: 0, y: 0, width: 136, height: 18, align: 'center', verticalAlign: 'middle',
  fontSize: 14, fill: '#718096'
};

const anchors = computed(() => [
  { id: 'top', x: props.config.width / 2, y: 0, radius: 5, fill: '#3182CE', opacity: isHovered.value ? 1 : 0, perfectDrawEnabled: false, listening: isHovered.value },
  { id: 'bottom', x: props.config.width / 2, y: boxHeight.value, radius: 5, fill: '#3182CE', opacity: isHovered.value ? 1 : 0, perfectDrawEnabled: false, listening: isHovered.value },
  { id: 'left', x: 0, y: boxHeight.value / 2, radius: 5, fill: '#3182CE', opacity: isHovered.value ? 1 : 0, perfectDrawEnabled: false, listening: isHovered.value },
  { id: 'right', x: props.config.width, y: boxHeight.value / 2, radius: 5, fill: '#3182CE', opacity: isHovered.value ? 1 : 0, perfectDrawEnabled: false, listening: isHovered.value }
]);

// --- Event Handlers ---
const onDragEnd = (e) => emit('update-position', { id: props.config.id, x: e.target.x(), y: e.target.y() });
const onDragMove = (e) => emit('update-position', { id: props.config.id, x: e.target.x(), y: e.target.y() });
const onAnchorClick = (e) => emit('anchor-click', { boxId: props.config.id, direction: e.target.id() });
const onContextMenu = (e) => emit('contextmenu', { event: e, id: props.config.id });

const addAttribute = () => emit('update-attribute', { boxId: props.config.id, index: null });
const addMethod = () => emit('update-method', { boxId: props.config.id, index: null });
const deleteAttribute = (index) => emit('delete-attribute', { boxId: props.config.id, index });
const deleteMethod = (index) => emit('delete-method', { boxId: props.config.id, index });

// --- In-place Editing Logic ---
function startEditing(type, index, event) {
  const textNode = event.target;
  const stage = textNode.getStage();
  const textPosition = textNode.getAbsolutePosition();
  const stageBox = stage.container().getBoundingClientRect();

  // 1. 기존 텍스트 숨기기
  textNode.hide();

  // 2. textarea 생성 및 스타일링
  const textarea = document.createElement('textarea');
  document.body.appendChild(textarea);

  // 클래스 제목(verticalAlign: 'middle')의 경우 위치 보정
  let yPos = textPosition.y;
  if (textNode.verticalAlign() === 'middle') {
    const textHeight = textNode.height();
    const actualTextHeight = textNode.getTextHeight();
    yPos += (textHeight - actualTextHeight) / 2;
  }

  textarea.value = textNode.text();
  textarea.style.position = 'absolute';
  textarea.style.top = stageBox.top + yPos + 'px';
  textarea.style.left = stageBox.left + textPosition.x + 'px';
  // textarea의 너비와 높이를 Konva 텍스트 노드와 동일하게 설정
  const textWidth = textNode.width();
  textarea.style.width = textWidth + 'px';
  textarea.style.height = (textNode.getTextHeight() + 2) + 'px'; // 실제 텍스트 높이 + 2px 여유 공간
  textarea.style.fontSize = textNode.fontSize() + 'px';
  textarea.style.border = '1px solid #4ba7fa';
  textarea.style.padding = textNode.padding() + 'px';
  textarea.style.boxSizing = 'border-box';
  textarea.style.margin = '0';
  textarea.style.whiteSpace = 'pre'; // 'nowrap' 대신 'pre'를 사용하여 공백 유지
  textarea.style.overflow = 'hidden'; // 스크롤바를 완전히 숨깁니다.
  textarea.style.background = 'white';
  textarea.style.outline = 'none';
  textarea.style.resize = 'none';
  textarea.style.lineHeight = '1'; // Konva Text의 기본 line-height와 일치시킵니다.
  textarea.style.fontFamily = textNode.fontFamily();
  textarea.rows = 1; // textarea가 한 줄 입력 필드처럼 동작하도록 합니다.
  textarea.style.transformOrigin = 'left top';
  textarea.style.textAlign = textNode.align();
  textarea.style.color = textNode.fill();
  textarea.style.zIndex = '1002';

  textarea.focus();
  textarea.select();

  const finishEditing = () => {
    // 3. 값 업데이트 및 textarea 제거
    const newValue = textarea.value.trim();
    if (newValue) {
      const emitEventMap = {
        'name': 'update-name',
        'attribute': 'update-attribute',
        'method': 'update-method'
      };
      // 높이 캐시 업데이트
      if (type === 'attribute' || type === 'method') {
        const cacheKey = `${type === 'attribute' ? 'attr' : 'method'}-${index}`;
        textHeightCache.value[cacheKey] = measureTextNodeHeight(newValue, textWidth, textNode.fontSize());
      }

      emit(emitEventMap[type], { boxId: props.config.id, index: index === -1 ? null : index, value: newValue });
    }
    document.body.removeChild(textarea);
    textNode.show();
  };

  textarea.addEventListener('blur', finishEditing);
  textarea.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      textarea.blur();
    }
    if (e.key === 'Escape') {
      textarea.value = textNode.text(); // 변경 취소
      textarea.blur();
    }
  });
}
</script>
