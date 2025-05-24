<template>
  <ul class="notice-board">
    <li
      v-for="notice in notices"
      :key="notice.noticeId"
      class="notice-card"
      @click="$emit('selectNotice', notice)"
    >
      <div class="card-header">
        <strong class="notice-title">{{ notice.title }}</strong>
      </div>
      <p class="notice-meta">
        {{ notice.writerName }}
        <span class="meta-date">{{ formatDateFull(notice.createdDate) }}</span>
      </p>
    </li>
  </ul>
</template>

<script setup>
defineProps({ notices: Array })
defineEmits(['selectNotice'])

function formatDateFull(dateStr) {
  const date = new Date(dateStr)
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  return `${yyyy}.${mm}.${dd}`
}
</script>

<style scoped>
.notice-board {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.notice-card {
  background: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  cursor: pointer;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  overflow: hidden;
  max-height: 70px;
}

.notice-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2px;
}

.notice-title {
  font-size: 16px;
  color: #2c3e50;
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-meta {
  font-size: 12px;
  color: #555;
  margin: 0; /* 간격 제거 */
}

.meta-date {
  margin-left: 2px;
  color: #999;
}
</style>
