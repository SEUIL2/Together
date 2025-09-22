<template>
  <div class="context-menu" :style="{ top: y + 'px', left: x + 'px' }">
    <!-- Message Type Change -->
    <template v-if="target.type === 'message'">
      <div class="menu-item" @click="updateType('sync')" :class="{active: target.currentType === 'sync'}">동기 메시지</div>
      <div class="menu-item" @click="updateType('async')" :class="{active: target.currentType === 'async'}">비동기 메시지</div>
      <div class="menu-item" @click="updateType('reply')" :class="{active: target.currentType === 'reply'}">응답 메시지</div>
    </template>

    <!-- Separator -->
    <div v-if="target.type === 'message'" class="menu-separator"></div>

    <!-- Delete -->
    <div class="menu-item danger" @click="deleteItem">🗑️ 삭제</div>
  </div>
</template>

<script setup>
const props = defineProps({
  x: Number,
  y: Number,
  target: Object,
});

const emit = defineEmits(['close', 'delete', 'update']);

const deleteItem = () => {
  emit('delete', props.target);
};

const updateType = (newType) => {
  emit('update', { type: 'message', id: props.target.id, newType });
};
</script>

<style scoped>
.context-menu {
  position: fixed;
  background: white;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  padding: 5px 0;
  min-width: 150px;
}
.menu-item {
  padding: 8px 15px;
  cursor: pointer;
  font-size: 14px;
}
.menu-item:hover {
  background-color: #f0f0f0;
}
.menu-item.danger {
  color: #e53935;
}
.menu-item.active {
  background-color: #e3f2fd;
  font-weight: bold;
}
.menu-separator {
  height: 1px;
  background-color: #eee;
  margin: 5px 0;
}
</style>