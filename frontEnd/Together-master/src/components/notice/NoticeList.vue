<template>
  <div class="notice-list">
    <div
      class="notice-item"
      v-for="notice in notices"
      :key="notice.noticeId"
      @click="$emit('selectNotice', notice)"
    >
      <h4 class="notice-title">{{ notice.title }}</h4>
      <div class="notice-meta">
        <span class="notice-writer">✍️ {{ notice.writerName }}</span>
        <span class="notice-date">📅 {{ formatDate(notice.createdDate) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  notices: Array
})
defineEmits(['selectNotice'])

function formatDate(dateString) {
  const date = new Date(dateString)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
}
</script>

<style scoped>
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 400px;
  overflow-y: auto;
  padding: 5px;
   overflow-y: auto;
     /* ✨ 스크롤바 숨김 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}
.notice-list::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Opera */

}

.notice-item {
  padding: 10px 14px;
  border-radius: 8px;
  background: #fdfdfd;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  border: 1px solid #e5e5e5;
  cursor: pointer;
  transition: all 0.15s ease;
}
.notice-item:hover {
  background: #f0f4ff;
  box-shadow: 0 3px 6px rgba(0, 123, 255, 0.15);
}

.notice-title {
  font-size: 15px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 12px;
  margin-top: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.notice-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #666;
  margin-top: -4px; /* ✨ 제목과 간격 좁힘 */
}
</style>
