<template>
  <v-group
    :config="{ id: config.id, x: config.x, y: config.y }"
    @dragend="onDragEnd"
    @contextmenu="onRightClick"
  >
    <!-- 드래그 가능한 그룹 (아이콘 + 이름) -->
    <v-group :config="{ draggable: true, name: 'draggable-group' }" @dblclick="onDblClick" @contextmenu="onRightClick">
      <!-- 액터 아이콘 -->
      <v-group v-if="config.type === 'actor'" :config="{ x: -20, y: 0 }">
        <v-circle :config="{ x: 20, y: 10, radius: 10, stroke: '#333', strokeWidth: 2 }" />
        <v-line :config="{ points: [20, 20, 20, 40], stroke: '#333', strokeWidth: 2 }" />
        <v-line :config="{ points: [5, 30, 35, 30], stroke: '#333', strokeWidth: 2 }" />
        <v-line :config="{ points: [20, 40, 5, 60], stroke: '#333', strokeWidth: 2 }" />
        <v-line :config="{ points: [20, 40, 35, 60], stroke: '#333', strokeWidth: 2 }" />
      </v-group>

      <!-- 객체(박스) 아이콘 -->
      <v-rect
        v-else
        :config="{
          x: -50,
          y: 0,
          width: 100,
          height: 40,
          fill: '#fff',
          stroke: '#333',
          strokeWidth: 2,
          cornerRadius: 4,
        }"
      />

      <!-- 이름 텍스트 -->
      <v-text
        :config="{
          text: config.name,
          x: -50,
          y: config.type === 'actor' ? 70 : 10,
          width: 100,
          align: 'center',
          fontSize: 14,
          fontStyle: 'bold',
          listening: true, // 더블 클릭 이벤트를 감지하도록 추가
        }"
      />
    </v-group>

    <!-- 생명선 (점선) -->
    <v-line
      :config="{
        points: [0, config.type === 'actor' ? 90 : 40, 0, canvasHeight],
        stroke: '#333',
        strokeWidth: 2,
        dash: [8, 6],
        name: 'lifeline-stem',
        lifelineId: config.id,
      }"
    />

    <!-- 점선 길이 조절 핸들 -->
    <v-circle
      :config="{
        x: 0,
        y: canvasHeight,
        radius: 8,
        fill: 'transparent',
        draggable: true,
        name: 'resize-handle',
      // 드래그 시작 시 핸들의 위치를 (0,0)으로 리셋하여 y()가 순수한 이동 거리가 되도록 함
      dragBoundFunc: function(pos) { return { x: 0, y: pos.y }; }
      }"
      @dragstart="onResizeStart"
      @dragmove="onResize"
      @dragend="onResizeEnd"
      @mouseenter="onResizeHandleEnter"
      @mouseleave="onResizeHandleLeave"
    />
  </v-group>
</template>

<script setup>
import { ref } from 'vue';

const props = defineProps({
  config: Object,
  canvasHeight: Number,
});
const emit = defineEmits(['update-position', 'update-height', 'contextmenu', 'dblclick']);

const onDragEnd = (e) => {
  // 드래그가 끝나면 Konva 노드를 가져옵니다.
  // 드래그가 끝나면 부모의 y좌표와 내부 그룹의 y좌표를 합산하여 최종 위치를 전달
  const group = e.target.getStage().findOne(`#${props.config.id}`);
  const draggableGroup = group.findOne('.draggable-group');
  emit('update-position', props.config.id, group.x() + draggableGroup.x(), group.y() + draggableGroup.y());
  // 내부 그룹의 위치를 리셋하여 중복 이동 방지
  draggableGroup.position({ x: 0, y: 0 });
};

const initialHeight = ref(0);

const onResizeStart = (e) => {
  // 드래그 시작 시점의 높이를 저장합니다.
  initialHeight.value = props.canvasHeight;
  // 드래그가 끝난 후 핸들의 위치가 (0,0)이 아니면 다음 드래그 계산이 꼬이므로 리셋합니다.
  e.target.position({ x: 0, y: 0 });
};

const onResize = (e) => {
  const handle = e.target;
  // 드래그 시작 높이(initialHeight)에 마우스 이동 거리(handle.y())를 더해 새로운 높이를 계산합니다.
  const newHeight = initialHeight.value + handle.y();
  emit('update-height', props.config.id, newHeight); // ID와 함께 전달
};

const onResizeEnd = () => {
  initialHeight.value = 0; // 드래그가 끝나면 초기화합니다.
};

const onResizeHandleEnter = (e) => {
  e.target.getStage().container().style.cursor = 'ns-resize';
};

const onResizeHandleLeave = (e) => {
  e.target.getStage().container().style.cursor = 'default';
};

const onRightClick = (e) => {
  emit('contextmenu', props.config.id, e);
};

const onDblClick = (e) => {
  emit('dblclick', e);
};
</script>