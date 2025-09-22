<template>
  <v-group @contextmenu="onRightClick" @dblclick="onDblClick">
    <!-- Message Line -->
    <v-arrow
      v-if="!isSelf"
      :config="{
        points: [fromPos.x, config.y, toPos.x, config.y],
        stroke: '#333',
        strokeWidth: 1.5,
        fill: isSync ? '#333' : (isReply ? '#fff' : '#333'),
        pointerLength: 10,
        pointerWidth: 8,
        dash: isReply ? [6, 4] : [],
        pointerAtBeginning: isReply,
      }"
    />

    <!-- Message Label -->
    <v-text
      :config="{
        text: config.name,
        x: isSelf ? fromPos.x + 5 : (fromPos.x + toPos.x) / 2 - 50,
        y: config.y - 20,
        width: 100,
        align: isSelf ? 'left' : 'center',
        fontSize: 13,
      }"
    />

    <!-- Self-Message specific parts -->
    <template v-if="isSelf">
      <v-line
        :config="{
          points: [
            fromPos.x, config.y,
            fromPos.x + 40, config.y,
            fromPos.x + 40, config.y + 20,
            fromPos.x, config.y + 20
          ],
          stroke: '#333',
          strokeWidth: 1.5,
        }"
      />
      <v-arrow
        :config="{
          points: [fromPos.x + 5, config.y + 20, fromPos.x, config.y + 20],
          stroke: '#333',
          strokeWidth: 1.5,
          fill: '#333',
          pointerLength: 10,
          pointerWidth: 8,
        }"
      />
    </template>
  </v-group>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
  config: { type: Object, required: true },
  lifelines: { type: Array, required: true },
});

const emit = defineEmits(['contextmenu', 'dblclick']);

const fromPos = computed(() => props.lifelines.find(l => l.id === props.config.from) || { x: 0 });
const toPos = computed(() => props.lifelines.find(l => l.id === props.config.to) || { x: 0 });

const isSync = computed(() => props.config.type === 'sync');
const isAsync = computed(() => props.config.type === 'async');
const isReply = computed(() => props.config.type === 'reply');
const isSelf = computed(() => props.config.from === props.config.to);

const onRightClick = (e) => {
  emit('contextmenu', props.config, e);
};

const onDblClick = (e) => {
  emit('dblclick', e);
};
</script>