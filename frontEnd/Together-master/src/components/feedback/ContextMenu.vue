<template>
  <div
    ref="menuRef"
    class="context-menu"
    :style="{ top: y + 'px', left: x + 'px' }"
    v-if="visible"
  >
    <ul>
      <li @click="handleMenuClick('add-feedback')">
        <span class="icon">📝</span>
        <span>피드백 작성</span>
      </li>
      <!-- 다른 메뉴 항목을 여기에 추가할 수 있습니다. -->
    </ul>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';

const props = defineProps({
  x: Number,
  y: Number,
  visible: Boolean,
});

const emit = defineEmits(['select', 'close']);

const menuRef = ref(null);

const handleMenuClick = (action) => {
  emit('select', action);
  emit('close');
};

const handleClickOutside = (event) => {
  if (menuRef.value && !menuRef.value.contains(event.target)) {
    emit('close');
  }
};

onMounted(() => {
  document.addEventListener('click', handleClickOutside, true);
});

onUnmounted(() => {
  document.removeEventListener('click', handleClickOutside, true);
});
</script>

<style scoped>
.context-menu {
  position: absolute;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 1000;
  padding: 6px;
  min-width: 180px;
}
ul {
  list-style: none;
  padding: 0;
  margin: 0;
}
li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  cursor: pointer;
  font-size: 14px;
  border-radius: 6px;
  transition: background-color 0.2s;
}
li:hover {
  background: #f5f5f5;
}
.icon {
  font-size: 16px;
}
</style>