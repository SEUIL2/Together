<template>
  <div class="notice-board">
    <h4>전체 공지사항</h4>
    <ul class="notice-list">
      <li
        v-for="notice in notices"
        :key="notice.noticeId"
        class="notice-card"
        @click="$emit('selectNotice', notice)"
      >
        <div class="notice-header">
          <strong class="notice-title">{{ notice.title }}</strong>
          <span class="notice-date">{{ formatDate(notice.createdDate) }}</span>
        </div>
        <p class="notice-writer">작성자: {{ notice.writerName }}</p>
        <p class="notice-content">{{ notice.content }}</p>
      </li>
    </ul>
  </div>
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
.notice-board {
  max-height: 80vh;
  overflow-y: auto;
  scrollbar-width: none;
}
.notice-board::-webkit-scrollbar {
  display: none;
}

.notice-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.notice-card {
  background: #fafafa;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
  cursor: pointer;
}

.notice-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.notice-title {
  font-size: 18px;
  color: #2c3e50;
}

.notice-date {
  font-size: 13px;
  color: #999;
  white-space: nowrap;
}

.notice-writer {
  font-size: 13px;
  color: #555;
  font-style: italic;
  margin-bottom: 8px;
}

.notice-content {
  font-size: 15px;
  color: #444;
  line-height: 1.6;
  white-space: pre-line;
}
</style>
