<template>
  <div class="page-node-container">
    <Handle type="target" :position="Position.Top" />
    <div class="node-content">
      <div class="node-label">{{ data.label }}</div>
    </div>
    <Handle type="source" :position="Position.Bottom" />
    <Handle type="source" :position="Position.Left" />
    <Handle type="source" :position="Position.Right" />
  </div>
</template>

<script setup>
import { Handle, Position } from '@vue-flow/core'

defineProps({
  data: {
    type: Object,
    required: true,
  },
})
</script>

<style scoped>
.page-node-container {
  padding: 20px 30px;
  border: 2px solid #a0aec0;
  border-radius: 8px;
  background: #f7fafc;
  min-width: 150px;
  min-height: 80px;
  display: flex;
  justify-content: center;
  align-items: center;
  text-align: center;
}

.node-label {
  font-weight: 600;
  color: #2d3748;
}
</style>