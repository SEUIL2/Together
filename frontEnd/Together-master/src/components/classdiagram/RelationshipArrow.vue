<template>
  <v-line
    :points="linePoints"
    :stroke="lineStyle.stroke"
    :strokeWidth="lineStyle.strokeWidth"
    :dash="lineStyle.dash"
    :hitStrokeWidth="20"
    @click="handleClick"
    @contextmenu="handleRightClick"
  />
  <!-- from쪽 도형 -->
  <component
    v-if="fromShapeComponent"
    :is="fromShapeComponent"
    v-bind="fromShapeProps"
  />
  <!-- to쪽 도형 -->
  <component
    v-if="toShapeComponent"
    :is="toShapeComponent"
    v-bind="toShapeProps"
  />
</template>

<script setup>
import { computed } from 'vue';
import { getConnectorPoints, getArrowheadProps } from '@/utils/relationshipUtils';
import TriangleHead from './shapes/TriangleHead.vue';
import ArrowHead from './shapes/ArrowHead.vue';
import DiamondHead from './shapes/DiamondHead.vue';

const props = defineProps({
  rel: Object,
  classes: Array,
  selected: Boolean
});

const emit = defineEmits(['select', 'open-menu']);

const linePoints = computed(() => getConnectorPoints(props.rel, props.classes));

const lineStyle = computed(() => {
  return {
    stroke: props.selected ? 'red' : '#333',
    strokeWidth: 2, // 두께는 2로 유지
    dash: props.rel.lineType === 'dashed' ? [6, 4] : [],  // 점선이면 [6, 4]로 처리
  };
});



function handleClick(e) {
  emit('select', { rel: props.rel, event: e });
}

function handleRightClick(e) {
  if (e.evt && typeof e.evt.preventDefault === 'function') {
    e.evt.preventDefault();
  }
  emit('open-menu', { rel: props.rel, event: e });
}

const shapeMap = {
  triangle: TriangleHead,
  arrow: ArrowHead,
  empty_diamond: DiamondHead,
  filled_diamond: DiamondHead,
  none: null
};

const fromShapeComponent = computed(() => shapeMap[props.rel.fromType || 'none']);
const toShapeComponent = computed(() => shapeMap[props.rel.toType || 'none']);

// 계산된 값으로 회전값을 180도 추가
const fromShapeProps = computed(() => {
  if (!fromShapeComponent.value) return {};
  const { x, y, rotation } = getArrowheadProps('from', props.rel, props.classes);
  return {
    x, y, rotation: rotation + 180,  // 180도 회전
    filled: props.rel.fromType === 'filled_diamond'
  };
});

const toShapeProps = computed(() => {
  if (!toShapeComponent.value) return {};
  const { x, y, rotation } = getArrowheadProps('to', props.rel, props.classes);
  return {
    x, y, rotation: rotation + 180,  // 180도 회전
    filled: props.rel.toType === 'filled_diamond'
  };
});

console.log('🔄 렌더링 관계선 fromType:', props.rel.fromType);
</script>

<style scoped>
</style>
