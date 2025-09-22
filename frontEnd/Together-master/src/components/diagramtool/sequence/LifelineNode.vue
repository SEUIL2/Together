<template>
  <v-group :x="config.x" :y="config.y" @contextmenu="onRightClick">
    <!-- Draggable Head Group -->
    <v-group :draggable="true" @dragend="onDragEnd">
      <!-- Head: Box or Actor -->
      <v-rect
        v-if="config.type === 'lifeline'"
        :config="{
          width: 120,
          height: 40,
          x: -60,
          y: -20,
          fill: '#fff',
          stroke: '#333',
          strokeWidth: 1.5,
          cornerRadius: 4,
        }"
      />
      <v-group v-if="config.type === 'actor'">
        <v-circle :config="{ x: 0, y: -25, radius: 15, stroke: '#333', strokeWidth: 1.5 }" />
        <v-line :config="{ points: [0, -10, 0, 20], stroke: '#333', strokeWidth: 1.5 }" />
        <v-line :config="{ points: [-20, 5, 20, 5], stroke: '#333', strokeWidth: 1.5 }" />
        <v-line :config="{ points: [0, 20, -15, 45], stroke: '#333', strokeWidth: 1.5 }" />
        <v-line :config="{ points: [0, 20, 15, 45], stroke: '#333', strokeWidth: 1.5 }" />
      </v-group>

      <!-- Name Text -->
      <v-text
        :config="{
          text: config.name,
          width: 120,
          x: -60,
          y: config.type === 'actor' ? -45 : -12,
          align: 'center',
          fontSize: 14,
          fontStyle: 'bold',
          fill: '#333',
        }"
        @dblclick="onDblClick"
      />
    </v-group>

    <!-- Stem (dashed line) -->
    <v-line
      :points="[0, config.type === 'actor' ? 45 : 20, 0, canvasHeight]"
      :stroke="'#555'"
      :strokeWidth="1.5"
      :dash="[4, 4]"
      :listening="true"
      :hitStrokeWidth="20"
      :name="'lifeline-stem'"
      :lifelineId="config.id"
    />
  </v-group>
</template>

<script setup>
const props = defineProps({
  config: { type: Object, required: true },
  canvasHeight: { type: Number, required: true },
});

const emit = defineEmits(['update-position', 'contextmenu', 'dblclick']);

const onDragEnd = (e) => {
  // 드래그 가능한 내부 그룹의 상대적 이동 거리를 부모 그룹의 절대 위치에 더해줍니다.
  const newX = props.config.x + e.target.x();
  const newY = props.config.y + e.target.y();
  emit('update-position', props.config.id, newX, newY);
  // 내부 그룹의 위치는 다시 (0,0)으로 리셋하여 위치가 중복 계산되지 않도록 합니다.
  e.target.x(0);
  e.target.y(0);
};

const onRightClick = (e) => {
  emit('contextmenu', props.config.id, e);
};

const onDblClick = (e) => {
  emit('dblclick', e);
};
</script>