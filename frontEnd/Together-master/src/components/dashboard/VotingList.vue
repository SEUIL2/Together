<template>
  <div class="voting-list-container">
<!-- 교수일 때만 헤더 보이게 -->
<div v-if="showHeader && userType === 'professor'" class="modal-header">
  <span class="title">🗳️ 팀 투표</span>
  <button class="create-button" @click="showCreateModal = true">+ 새 투표</button>
  <button class="close-btn" @click="close">×</button>
</div>


    <!-- 투표 목록 -->
    <div v-if="votes && votes.length" class="vote-cards">
      <div
        v-for="vote in votes"
        :key="vote.voteId"
        class="vote-card"
        @click="openDetail(vote.voteId)"
      >
        <div class="vote-header">
          <div class="vote-title">{{ vote.title }}</div>
          <div class="vote-dday" :class="{ closed: isDeadlinePassed(vote) }">
            {{ isDeadlinePassed(vote) ? '마감됨' : getDDay(vote) }}
          </div>
        </div>

        <div class="vote-meta">
          <div class="meta-item">
            <span class="meta-icon">👤</span>
            <span>{{ vote.anonymous ? '익명 투표' : vote.userName }}</span>
          </div>
          <div class="meta-item" v-if="vote.deadLine">
            <span class="meta-icon">⏰</span>
            <span>마감: {{ formatDate(vote.deadLine) }}</span>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="empty">투표가 없습니다. 새로 만들어보세요!</div>

    <!-- 모달 -->
<VoteCreateModal
  v-if="showCreateModal"
  :project-id="projectId" 
  @close="showCreateModal = false"
  @created="handleCreated"
/>

    <VotingDetailModal
      v-if="selectedVoteId"
      :vote-id="selectedVoteId"
      @close="selectedVoteId = null"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/api'
import VoteCreateModal from '@/components/dashboard/VoteCreateModal.vue'
import VotingDetailModal from '@/components/dashboard/VotingDetailModal.vue'

// ✅ props
const props = defineProps({
  projectId: Number,
  showHeader: { type: Boolean, default: true },
  userType: { type: String, default: 'student' } // 'professor' 또는 'student'
})


// ✅ emit
const emit = defineEmits(['close'])

// ✅ 상태
const votes = ref([])
const showCreateModal = ref(false)
const selectedVoteId = ref(null)

// ✅ 닫기 버튼
function close() {
  emit('close')
}

// ✅ 투표 목록 불러오기
async function fetchVotes() {
  try {
    const idToUse = props.projectId || Number(localStorage.getItem('currentProjectId'))
    const res = await api.get('/votes/project', {
      params: { projectId: idToUse },
      headers: { Authorization: localStorage.getItem('authHeader') },
    })
    // 최신순으로 정렬합니다.
    votes.value = res.data.sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate));
  } catch (err) {
    console.error('❌ 투표 목록 불러오기 실패:', err)
  }
}

// ✅ 투표 상세 열기
function openDetail(id) {
  selectedVoteId.value = id
}

// ✅ 마감 여부
function isDeadlinePassed(vote) {
  if (!vote.deadLine) return false
  return new Date() > new Date(vote.deadLine)
}

// ✅ D-day 계산
function getDDay(vote) {
  if (!vote.deadLine) return ''
  const deadline = new Date(vote.deadLine)
  const today = new Date()
  const diff = Math.ceil((deadline - today) / (1000 * 60 * 60 * 24))
  return diff >= 0 ? `D-${diff}` : '마감됨'
}

// ✅ 날짜 포맷
function formatDate(dateStr) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
}
function handleCreated() {
  showCreateModal.value = false
  setTimeout(fetchVotes, 300) // 살짝 delay 줘도 부드러움
}


// ✅ 초기 로드
onMounted(fetchVotes)
defineExpose({ fetchVotes })
</script>

<style scoped>
/* 컨테이너가 부모 카드 안에서 레이아웃을 꽉 채우고 내부 스크롤이 가능하도록 수정 */
.voting-list-container {
  /* 기존 고정 폭/가운데 정렬 제거 */
  /* max-width: 580px; */
  /* margin: auto; */

  width: 100%;
  height: 100%;
  min-width: 0;

  padding: 1rem 1.3rem 1.2rem 1.3rem;
  font-family: 'Pretendard', sans-serif;
  position: relative;

  /* 핵심: 카드(body) 안에서 남은 공간을 차지하고 내부 스크롤이 작동하도록 */
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
}

/* 헤더는 sticky로 상단 고정 (기존 유지) */
.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 0.85rem;
  border-bottom: 1.2px solid #eee;
  margin-bottom: 0.8rem;
  position: sticky;
  top: 0;
  background: #fff;
  z-index: 5;
}

.title {
  font-size: 1.2rem;
  font-weight: bold;
  color: #222;
  flex: 1 0 0;
}

.create-button {
  background-color: #4f46e5;
  color: white;
  padding: 0.37rem 1rem;
  border: none;
  border-radius: 7px;
  font-weight: 500;
  margin: 0 8px;
  cursor: pointer;
  font-size: 0.98rem;
  transition: background 0.17s;
}
.create-button:hover { background: #3745ae; }

.close-btn {
  background: none;
  border: none;
  font-size: 1.43rem;
  color: #888;
  cursor: pointer;
  margin-left: 4px;
  padding: 2px 9px;
  border-radius: 7px;
  transition: background 0.14s;
}
.close-btn:hover { background: #f0f0f6; color: #e23333; }

/* ▼ 스크롤이 필요한 목록 영역: 남는 공간을 차지하고 내부 스크롤 */
.vote-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;

  flex: 1 1 auto; /* 남은 공간 채우기 */
  min-height: 0;  /* 매우 중요: 이게 있어야 overflow가 작동함 */
  overflow: auto; /* 내부 스크롤 */
  padding-right: 4px; /* 스크롤바 겹침 여유 */
  /* 스크롤바 숨기기 */
  scrollbar-width: none;        /* Firefox */
  -ms-overflow-style: none;
}
.vote-cards::-webkit-scrollbar { /* Chrome, Safari */
  display: none;
}

/* 긴 제목/내용이 가로로 넘치지 않도록 안전장치 */
.voting-list-container * {
  min-width: 0;
  box-sizing: border-box;
  word-break: break-word;
}

.vote-card {
  padding: 1.15rem 1.3rem;
  border: 1px solid #e5e7ef;
  border-radius: 14px;
  background-color: #ffffff;
  cursor: pointer;
  transition: all 0.25s ease;
  box-shadow: 0 1px 3px rgba(80, 80, 80, 0.04);
}
.vote-card:hover {
  background-color: #f8faff;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
}

.vote-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.65rem;
}

.vote-title {
  font-weight: 700;
  font-size: 1.1rem;
  color: #222;

  /* 긴 제목 한 줄 말줄임 (선택) */
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.vote-dday {
  font-size: 0.82rem;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 12px;
  color: #3b49df;
  background-color: #edf0ff;
}
.vote-dday.closed { color: #aaa; background-color: #f2f2f2; }

.vote-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.93rem;
  color: #555;
}

.meta-item { display: flex; align-items: center; gap: 6px; }
.meta-icon { font-size: 0.95rem; }

.empty {
  text-align: center;
  padding: 2rem;
  color: #999;
  margin-top: .5rem; /* 헤더 바로 아래 자연스럽게 띄움 */
}
</style>
