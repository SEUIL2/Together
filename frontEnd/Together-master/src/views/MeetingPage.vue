<template>
  <div class="meeting-page">
    <!-- 왼쪽: 날짜 리스트 -->
    <aside class="sidebar">
      <h3>회의 목록</h3>
      <ul>
        <li
          v-for="(meeting, index) in meetings"
          :key="index"
          :class="{ active: selectedIndex === index }"
          @click="selectedIndex = index"
        >
          {{ meeting.date }}
        </li>
      </ul>
      <button class="add-btn" @click="addMeeting">회의 생성</button>
    </aside>

    <!-- 오른쪽: 회의 상세 -->
    <main class="content">
      <div class="header">
        <input
          v-model="meetings[selectedIndex].title"
          class="title-input"
          placeholder="제목을 입력하세요"
        />
        <p class="date">{{ meetings[selectedIndex].date }}</p>
        <button class="delete-btn" @click="deleteMeeting">삭제</button>
      </div>

      <!-- 마크다운 에디터 -->
      <v-md-editor
        v-if="meetings.length > 0"
        v-model="meetings[selectedIndex].content"
        height="70vh"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

// 회의 목록
const meetings = ref(
  JSON.parse(localStorage.getItem('meetings')) || [
    {
      date: '2025년 3월 26일',
      title: '첫 회의',
      content: '# 회의 내용을 작성하세요'
    }
  ]
)

const selectedIndex = ref(0)

function addMeeting() {
  const dateStr = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })

  meetings.value.push({
    date: dateStr,
    title: '새 회의',
    content: '# 새 회의 내용을 작성하세요'
  })

  selectedIndex.value = meetings.value.length - 1
  saveMeetings()
}

function deleteMeeting() {
  if (meetings.value.length <= 1) {
    alert('최소 하나의 회의는 남겨야 합니다.')
    return
  }
  meetings.value.splice(selectedIndex.value, 1)
  selectedIndex.value = 0
  saveMeetings()
}

function saveMeetings() {
  localStorage.setItem('meetings', JSON.stringify(meetings.value))
}

watch(meetings, saveMeetings, { deep: true })
</script>

<style scoped>
.meeting-page {
  display: flex;
  height: 100vh;
  background: #f5f5f5;
}

/* 사이드바 */
.sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #ddd;
  padding: 1rem;
}

.sidebar h3 {
  font-size: 1.1rem;
  margin-bottom: 1rem;
}

.sidebar ul {
  list-style: none;
  padding: 0;
}

.sidebar li {
  padding: 10px;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.2s;
}

.sidebar li:hover {
  background: #eee;
  transform: translateX(1px);
}

.sidebar li.active {
  background: #3f8efc;
  color: white;
}
.sidebar li.active:hover {
    background-color: #2674e0;
    transform: translateX(1px);
  }

.add-btn {
  width: 100%;
  padding: 8px;
  background: #adadad;
  color: white;
  border: none;
  border-radius: 4px;
  margin-top: 1rem;
  cursor: pointer;
}
.add-btn:hover {
    background-color: rgb(138, 138, 138);

  }

/* 오른쪽 회의 영역 */
.content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: #fafafa;
}

.header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
}

.title-input {
  font-size: 1.2rem;
  font-weight: bold;
  flex: 1;
  border: none;
  border-bottom: 1px solid #ccc;
  padding: 4px;
}

.date {
  font-size: 0.9rem;
  color: #666;
}

.delete-btn {
  background: transparent;
  border: none;
  font-size: 1rem;
  color: #f44336;
  cursor: pointer;
}
</style>
