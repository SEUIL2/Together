<template>
  <v-group
    :x="config.x"
    :y="config.y"
    :draggable="true"
    @dragend="onDragEnd"
    @contextmenu="onRightClick"
    @dblclick="onDblClick"
  >
    <!-- Main rectangle -->
    <v-rect
      ref="rectRef"
      :config="{
        x: 0,
        y: 0,
        width: config.width,
        height: config.height,
        stroke: '#333',
        strokeWidth: 1.5,
        dash: config.type === 'opt' || config.type === 'alt' ? [4, 4] : [],
      }"
    />
    <!-- Label tab -->
    <v-label :config="{ x: 0, y: 0 }">
      <v-tag
        :config="{
          fill: '#fff',
          stroke: '#333',
          strokeWidth: 1.5,
          lineJoin: 'round',
        }"
      />
      <v-text
        :config="{
          text: config.name,
          padding: 5,
          fontSize: 14,
          fontStyle: 'bold',
          listening: true, // 더블 클릭 이벤트를 감지하도록 추가
        }"
      />
    </v-label>

    <!-- Transformer for resizing -->
    <v-transformer ref="transformerRef" />
  </v-group>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue';

const props = defineProps({
  config: { type: Object, required: true },
});

const emit = defineEmits(['update', 'contextmenu', 'dblclick']);

const rectRef = ref(null);
const transformerRef = ref(null);

onMounted(() => {
  const transformer = transformerRef.value.getNode();
  const rect = rectRef.value.getNode();
  transformer.nodes([rect]);
  transformer.getLayer().batchDraw();

  rect.on('transformend', () => {
    const updatedConfig = {
      ...props.config,
      x: rect.x(),
      y: rect.y(),
      width: rect.width() * rect.scaleX(),
      height: rect.height() * rect.scaleY(),
    };
    rect.scaleX(1);
    rect.scaleY(1);
    emit('update', updatedConfig);
  });
});

watch(() => props.config, (newConfig) => {
  const rect = rectRef.value?.getNode();
  if (rect) {
    rect.x(newConfig.x);
    rect.y(newConfig.y);
    rect.width(newConfig.width);
    rect.height(newConfig.height);
  }
}, { deep: true });

const onDragEnd = (e) => {
  emit('update', {
    ...props.config,
    x: e.target.x(),
    y: e.target.y(),
  });
};

const onRightClick = (e) => {
  emit('contextmenu', props.config.id, e);
};

const onDblClick = (e) => {
  emit('dblclick', e); // 이벤트 객체를 부모로 전달
};
</script>