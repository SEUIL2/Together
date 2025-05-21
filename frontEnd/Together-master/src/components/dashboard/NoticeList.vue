<template>
  <ul class="notice-list">
    <li
      v-for="notice in notices"
      :key="notice.noticeId"
      class="notice-item"
      @click="$emit('selectNotice', notice)"
    >
      <div class="notice-content">
        <span class="notice-title">{{ notice.title }}</span>
        <span class="notice-date">{{ formatDate(notice.createdDate) }}</span>
      </div>
    </li>
  </ul>
</template>

<script setup>
defineProps({
  notices: Array
})
defineEmits(['selectNotice'])

function formatDate(dateStr) {
  const date = new Date(dateStr)
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  return `${month}월${day}일`
}
</script>

<style scoped>
.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-item {
  padding: 12px 4px;
  cursor: pointer;
  transition: background 0.2s ease, transform 0.2s ease;
  border-radius: 6px;
}

.notice-item:hover {
  background-color: #f5f7fa;
  transform: translateY(-1px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.notice-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notice-title {
  font-weight: bold;
  color: #333;
}

.notice-date {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
}
</style>
