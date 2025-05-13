<template>
  <div class="all-tasks-container">
    <div class="section-header">
      <h3 class="section-title">모든 작업</h3>
      <label class="checkbox-label">
        <input type="checkbox" v-model="hideCompleted" />
        작업완료 제외
      </label>
    </div>

    <div class="task-list">
      <div
        class="task-card"
        v-for="task in filteredTasks"
        :key="task.id"
      >
        <!-- 상단 -->
        <div class="card-top">
          <div class="task-title">{{ task.title }}</div>
          <div class="date-range">{{ formatDate(task.startDate) }} ~ {{ formatDate(task.endDate) }}</div>
        </div>

        <!-- 하단 -->
        <div class="card-bottom">
          <div class="assignee">👤 {{ task.assignedUserName || '미지정' }}</div>
          <div class="task-status">
            <span :class="'status-badge ' + task.status.toLowerCase()">
              {{ statusLabel(task.status) }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  tasks: Array
})

const hideCompleted = ref(false)

const filteredTasks = computed(() => {
  return hideCompleted.value
    ? props.tasks.filter(t => t.status !== 'COMPLETED')
    : props.tasks
})

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR')
}

const statusLabel = (status) => {
  switch (status) {
    case 'PENDING': return '작업전'
    case 'IN_PROGRESS': return '작업중'
    case 'COMPLETED': return '작업완료'
    default: return status
  }
}
</script>

<style scoped>
.all-tasks-container {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.section-title {
  margin: 0 0 4px 4px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.checkbox-label {
  font-size: 14px;
  color: #444;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  user-select: none;
  margin-right: 8px;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
  padding-right: 6px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.task-list::-webkit-scrollbar {
  display: none;
}

.task-card {
  background: #f9f9fc;
  border: 1px solid #ddd;
  border-radius: 8px;
  padding: 12px 16px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 14px;
}

.card-top, .card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.date-range {
  font-size: 13px;
  color: #666;
}

.assignee {
  color: #555;
}

.status-badge {
  padding: 4px 10px;
  font-size: 12px;
  font-weight: bold;
  border-radius: 12px;
  text-transform: none;
  background-color: #eee;
  color: #333;
}
.status-badge.pending {
  background-color: #f8d7da;
  color: #842029;
}
.status-badge.in_progress {
  background-color: #fff3cd;
  color: #664d03;
}
.status-badge.completed {
  background-color: #d1e7dd;
  color: #0f5132;
}
</style>
