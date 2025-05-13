<template>
  <div class="all-tasks-container">
    <div class="section-header">
      <h3 class="section-title">내 작업</h3>

      <div class="filters">
        <label class="checkbox-label">
          <input type="checkbox" v-model="hideCompleted" />
          작업완료 제외
        </label>

        <!-- 🔽 담당자 선택 드롭다운 -->
        <select v-model="selectedUser" class="assignee-select">
          <option value="">전체</option>
          <option 
            v-for="user in uniqueUsers" 
            :key="user" 
            :value="user"
          >
            {{ user || '미지정' }}
          </option>
        </select>
      </div>
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
import { ref, computed, watch } from 'vue'

const props = defineProps({
  tasks: Array,
  currentUserName: String  // 로그인된 사용자 이름
})

const hideCompleted = ref(false)
const selectedUser = ref('')

watch(() => props.currentUserName, (newVal) => {
  if (!selectedUser.value && newVal) {
    selectedUser.value = newVal
  }
}, { immediate: true })

// 고유한 담당자 목록 생성
const uniqueUsers = computed(() => {
  const names = props.tasks.map(task => task.assignedUserName || '미지정')
  return [...new Set(names)]
})

// 작업 필터링
const filteredTasks = computed(() => {
  return props.tasks.filter(task => {
    const notCompleted = !hideCompleted.value || task.status !== 'COMPLETED'
    const matchesUser = !selectedUser.value || (task.assignedUserName || '미지정') === selectedUser.value
    return notCompleted && matchesUser
  })
})

// 날짜 포맷
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR')
}

// 상태 표시
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
  flex-wrap: wrap;
}

.section-title {
  margin: 0 0 4px 4px;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.filters {
  display: flex;
  align-items: center;
  gap: 12px;
}

.checkbox-label {
  font-size: 14px;
  color: #444;
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  user-select: none;
}

.assignee-select {
  padding: 4px 8px;
  font-size: 14px;
  border-radius: 4px;
  border: 1px solid #ccc;
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
