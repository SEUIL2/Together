<template>
  <div class="info-tooltip-wrapper" @mouseenter="show = true" @mouseleave="show = false">
    <span class="info-icon">ⓘ</span>
    <div v-if="show && text" class="tooltip-content">
      {{ text }}
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';

defineProps({
  text: String,
});

const show = ref(false);
</script>

<style scoped>
.info-tooltip-wrapper {
  position: relative;
  display: inline-flex;
  align-items: center;
  margin-left: 8px;
}

.info-icon {
  cursor: pointer;
  color: #9ca3af;
  font-weight: bold;
  font-size: 14px;
 
  border-radius: 50%;
  width: 18px;
  height: 18px;
  display: inline-flex;
  justify-content: center;
  align-items: center;
  line-height: 1;
  transition: color 0.2s, border-color 0.2s;
}

.info-tooltip-wrapper:hover .info-icon {
  color: #3b82f6;
  border-color: #3b82f6;
}

.tooltip-content {
  position: absolute;
  top: 50%;
  left: 100%;
  transform: translateY(-50%);
  margin-left: 12px;
  background-color: #2d3748;
  color: white;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  white-space: pre-wrap;
  z-index: 9999;
  width: max-content;
  max-width: 300px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  pointer-events: none;
  opacity: 0;
  animation: fadeIn 0.2s ease forwards;
}

@keyframes fadeIn {
  to {
    opacity: 1;
  }
}
</style>