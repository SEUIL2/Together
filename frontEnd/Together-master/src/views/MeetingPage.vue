<template>
  <div class="meeting-page">
    <aside class="sidebar">
      <div class="sidebar-header">
        <h3>회의 목록</h3>
        <select v-model="selectedFilter" class="category-filter">
          <option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option>
        </select>
      </div>
      <div class="add-btn-container">
        <button class="add-btn" @click="addMeeting">회의 생성</button>
      </div>
      <ul>
        <li
            v-for="(meeting, index) in filteredMeetings"
            :key="meeting.meetingId"
            :class="{ active: selectedIndex === index }"
            @click="selectedIndex = index"
        >
          <span class="list-item-title">{{ meeting.title }}</span>
          <div class="list-item-meta">
            <span class="list-item-date">{{ formatDate(meeting.meetingDate) }}</span>
            <span v-if="meeting.category" class="category-badge" :style="{ backgroundColor: categoryColors[meeting.category] }">
              {{ categories[meeting.category] }}
            </span>
          </div>
        </li>
      </ul>
    </aside>

    <main class="content" v-if="currentMeeting">
      <div class="header">
        <input
            v-model="currentMeeting.title"
            class="title-input"
            placeholder="제목을 입력하세요"
            @blur="() => saveMeeting(currentMeeting)"
        />
        <select v-model="currentMeeting.category" @change="() => saveMeeting(currentMeeting)" class="category-select">
          <option v-for="(label, key) in detailCategories" :key="key" :value="key">{{ label }}</option>
        </select>
        <p class="date">{{ formatDate(currentMeeting.meetingDate) }}</p>
        <button class="delete-btn" @click="deleteMeeting">삭제</button>
      </div>

      <v-md-editor
          v-model="currentMeeting.content"
          height="70vh"
          @blur="() => saveMeeting(currentMeeting)"
      />
    </main>

    <main v-else class="content empty-content">
      <p v-if="meetings.length > 0 && filteredMeetings.length === 0">선택된 카테고리에 회의가 없습니다.</p>
      <p v-else>회의가 없습니다. 왼쪽에서 회의를 추가해주세요!</p>
    </main>
  </div>
</template>

<script setup>
import {ref, onMounted, watch, computed, nextTick} from 'vue'
import api from '@/api/meetingApi'
import {useRouter} from 'vue-router'

const router = useRouter()
const meetings = ref([])
const selectedIndex = ref(0)
const selectedFilter = ref('ALL')

const categories = {
  ALL: '전체',
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트'
}

const categoryColors = {
  PLANNING: '#ffaeae',
  DESIGN: '#f39c12',
  DEVELOPMENT: '#2ecc71',
  TEST: '#9b59b6'
}

const detailCategories = computed(() => {
  const {ALL, ...rest} = categories
  return rest
})

const filteredMeetings = computed(() => {
  if (selectedFilter.value === 'ALL') {
    return meetings.value
  }
  return meetings.value.filter(m => m.category === selectedFilter.value)
})

const currentMeeting = computed(() => {
  if (filteredMeetings.value.length === 0 || selectedIndex.value < 0) return null
  return filteredMeetings.value[selectedIndex.value]
})

watch(selectedFilter, () => {
  selectedIndex.value = 0
})

// ✅ 날짜 포맷
const formatDate = (dateStr) => {
  if (!dateStr) return ''
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
    const response = await api.get('/all-author');
    meetings.value = response.data.sort((a, b) => new Date(b.meetingDate) - new Date(a.meetingDate));
    if (meetings.value.length > 0 && selectedIndex.value >= filteredMeetings.value.length) {
      selectedIndex.value = 0
    }
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
    meetingDate: now.toISOString(),
    category: 'PLANNING'
  }

  try {
    const response = await api.post('/create', meetingDto);
    meetings.value.unshift(response.data); // 새 회의를 배열 맨 앞에 추가
    // meetings.value.sort((a, b) => new Date(b.meetingDate) - new Date(a.meetingDate)); // unshift를 사용하므로 이 줄은 필요 없어집니다.
    selectedFilter.value = 'ALL';
    await nextTick();
    selectedIndex.value = meetings.value.findIndex(m => m.meetingId === response.data.meetingId);
  } catch (err) {
    console.error('회의 생성 실패:', err)
  }
}

// ✅ 회의 삭제
async function deleteMeeting() {
  if (!currentMeeting.value) return

  if (meetings.value.length <= 1) {
    alert('최소 하나의 회의는 남겨야 합니다.')
    return
  }

  const meetingToDelete = currentMeeting.value
  const currentIndex = selectedIndex.value

  try {
    await api.delete(`/delete/${meetingToDelete.meetingId}`)
    meetings.value = meetings.value.filter(m => m.meetingId !== meetingToDelete.meetingId)

    if (currentIndex >= filteredMeetings.value.length) {
      selectedIndex.value = Math.max(0, filteredMeetings.value.length - 1)
    }
  } catch (err) {
    console.error('회의 삭제 실패:', err)
  }
}

// ✅ 회의 저장
async function saveMeeting(meeting) {
  if (!meeting) return
  try {
    const payload = {...meeting, updatedAt: new Date().toISOString()}
    await api.put(`/update/${meeting.meetingId}`, payload)
    // console.log('저장됨:', meeting)
  } catch (err) {
    console.error('회의 저장 실패:', err)
  }
}

// ✅ 처음 로딩 시 회의 불러오기
onMounted(() => {
  fetchMeetings()
})
</script>


<style scoped>
.meeting-page {
  display: flex;
  height: 92vh;
  background: #f8f9fa;
}

/* 사이드바 */
.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #ddd;
  padding: 1rem;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding-bottom: 0.5rem;
  margin-bottom: 0.5rem;
  margin-top: -10%;
}

.add-btn-container {
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.sidebar h3 {
  font-size: 1.1rem;
  margin-bottom: 0;
}

.category-filter {
  width: auto;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

.sidebar ul {
  list-style: none;
  padding: 0;
  overflow-y: auto;
  flex-grow: 1;
}

.sidebar li {
  padding: 12px 10px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.list-item-title {
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.list-item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-item-date {
  font-size: 0.8rem;
  color: #888;
}

.category-badge {
  font-size: 0.75rem;
  padding: 1.5px 8px;
  border-radius: 10px;
  color: white;
  font-weight: 500;
}

.sidebar li:hover {
  background: #eee;
  transform: translateX(1px);
}

.sidebar li.active {
  background: #a7ccff;
  color: white;
}

.sidebar li.active:hover {
  background-color: #97c3ff;
  transform: translateX(1px);
}

.add-btn {
  width: 100%;
  padding: 10px;
  background: #3f8efc;
  color: white;
  border: none;
  border-radius: 4px;
  flex-shrink: 0; /* 버튼 크기가 줄어들지 않도록 고정 */
  cursor: pointer;
}

.add-btn:hover {
  background-color: #3578e5;

}

/* 오른쪽 회의 영역 */
.content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: #fff;
}

.empty-content {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #888;
}

.header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #eee;
}

.title-input {
  font-size: 1.5rem;
  font-weight: bold;
  flex: 1;
  border: none;
  padding: 4px;
  background: transparent;
}

.title-input:focus {
  outline: none;
}

.category-select {
  padding: 4px 8px;
  border-radius: 6px;
  border: 1px solid #ccc;
}

.date {
  font-size: 0.9rem;
  color: #666;
  white-space: nowrap;
}

.delete-btn {
  background: transparent;
  border: none;
  font-size: 1rem;
  color: #f44336;
  cursor: pointer;
  transition: color 0.2s;
}

.delete-btn:hover {
  color: #d32f2f;
}
</style>