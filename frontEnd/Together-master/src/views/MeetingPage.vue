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
          {{ meeting.date || formatDate(meeting.meetingDate) }}
        </li>
      </ul>
      <button class="add-btn" @click="addMeeting">회의 생성</button>
    </aside>

    <!-- 오른쪽: 회의 상세 -->
    <main class="content" v-if="meetings.length > 0">
      <div class="header">
        <input
          v-model="meetings[selectedIndex].title"
          class="title-input"
          placeholder="제목을 입력하세요"
          @blur="() => saveMeeting(meetings[selectedIndex])"
        />
        <p class="date">{{ formatDate(meetings[selectedIndex].meetingDate) }}</p>
        <button class="delete-btn" @click="deleteMeeting">삭제</button>
      </div>

      <!-- 마크다운 에디터 -->
      <v-md-editor
        v-model="meetings[selectedIndex].content"
        height="70vh"
        @blur="() => saveMeeting(meetings[selectedIndex])"
      />
    </main>

    <!-- 로딩 또는 데이터 없을 때 -->
    <main v-else class="content">
      <p>회의가 없습니다. 왼쪽에서 회의를 추가해주세요!</p>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import api from '@/api/meetingApi'

const meetings = ref([])
const selectedIndex = ref(0)

// ✅ 날짜 포맷
const formatDate = (dateStr) => {
  const date = new Date(dateStr)
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

// ✅ 회의 목록 불러오기
async function fetchMeetings() {
  try {
    const response = await api.get('/all-author')
    meetings.value = response.data
  } catch (err) {
    console.error('회의 목록 불러오기 실패:', err)
  }
}

// ✅ 회의 생성
async function addMeeting() {
  const now = new Date()
  const meetingDto = {
    title: '새 회의',
    content: '# 새 회의 내용을 작성하세요',
    meetingDate: now,
    createdAt: now,
    updatedAt: now
  }

  try {
    const response = await api.post('/create', meetingDto)
    meetings.value.push(response.data)
    selectedIndex.value = meetings.value.length - 1
  } catch (err) {
    console.error('회의 생성 실패:', err)
  }
}

// ✅ 회의 삭제
async function deleteMeeting() {
  if (meetings.value.length <= 1) {
    alert('최소 하나의 회의는 남겨야 합니다.')
    return
  }

  const target = meetings.value[selectedIndex.value]

  try {
    await api.delete(`/delete/${target.meetingId}`)
    meetings.value.splice(selectedIndex.value, 1)
    selectedIndex.value = 0
  } catch (err) {
    console.error('회의 삭제 실패:', err)
  }
}

// ✅ 회의 저장
async function saveMeeting(meeting) {
  try {
    meeting.updatedAt = new Date()
    await api.put(`/update/${meeting.meetingId}`, meeting)
    // console.log('저장됨:', meeting)
  } catch (err) {
    console.error('회의 저장 실패:', err)
  }
}

// ✅ 무한 루프 방지용 watcher
let previousContent = ''

watch(meetings, (newMeetings) => {
  const current = newMeetings[selectedIndex.value]
  if (!current || !current.meetingId) return

  const currentContent = JSON.stringify(current)
  if (currentContent !== previousContent) {
    previousContent = currentContent
    saveMeeting(current)
  }
}, { deep: true })

// ✅ 처음 로딩 시 회의 불러오기
onMounted(() => {
  fetchMeetings()
})
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
