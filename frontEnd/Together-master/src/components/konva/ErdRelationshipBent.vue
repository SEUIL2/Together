<template>
  <v-group>
    <v-line
      :points="[from.x, from.y, mid.x, mid.y, to.x, to.y]"
      :stroke="getStrokeColor()"
      :strokeWidth="2"
      :dash="lineStyle === 'dashed' ? [6, 4] : []"
      @contextmenu.prevent="onRightClick"
    />
  </v-group>
</template>

<script setup>
const props = defineProps({
  from: Object,
  to: Object,
  mid: Object,
  lineStyle: String,
  rel: Object,
});
const emit = defineEmits(['open-context']);

const onRightClick = (e) => {
  emit('open-context', { rel: props.rel, event: e.evt });
};

const getStrokeColor = () => {
  return '#333';
};
</script>
