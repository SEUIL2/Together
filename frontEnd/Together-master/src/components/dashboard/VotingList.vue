<template>
  <div class="voting-list-container">
    <!-- 헤더: showHeader가 true일 때만 보임 -->
    <div v-if="showHeader" class="modal-header">
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
        <div class="vote-title">{{ vote.title }}</div>
        <div class="vote-info">
          <span>{{ vote.voteItems?.length ?? 0 }}개 항목</span>
          <span>· {{ vote.voteResponseEntitys?.length ?? 0 }}명 참여</span>
        </div>
        <div class="vote-status" :class="{ closed: isDeadlinePassed(vote) }">
          {{ isDeadlinePassed(vote) ? '마감됨' : getDDay(vote) }}
        </div>
      </div>
    </div>

    <div v-else class="empty">투표가 없습니다. 새로 만들어보세요!</div>

    <!-- 모달들 -->
    <VoteCreateModal v-if="showCreateModal" @close="showCreateModal = false" @created="fetchVotes" />
    <VotingDetailModal
      v-if="selectedVoteId"
      :vote-id="selectedVoteId"
      @close="selectedVoteId = null"
    />
  </div>
</template>



<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import VoteCreateModal from '@/components/dashboard/VoteCreateModal.vue'
import VotingDetailModal from '@/components/dashboard/VotingDetailModal.vue'

const votes = ref([])
const showCreateModal = ref(false)
const selectedVoteId = ref(null)
const projectId = Number(localStorage.getItem('currentProjectId')) // 또는 props로 받기

// 부모에게 닫기 emit (모달 상위에서 @close="showVotingModal = false" 해줘야 함)
const emit = defineEmits(['close'])
function close() {
  emit('close')
}

async function fetchVotes() {
  try {
    const res = await axios.get('/votes/project', {
      params: { projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
    })
    votes.value = res.data
  } catch (err) {
    console.error('❌ 투표 목록 불러오기 실패:', err)
  }
}

function openDetail(id) {
  console.log('voteId:', id)
  selectedVoteId.value = id
}


function getDDay(vote) {
  if (!vote.createdDate || !vote.durationDays) return ''
  const created = new Date(vote.createdDate)
  const deadline = new Date(created)
  deadline.setDate(deadline.getDate() + vote.durationDays)
  const today = new Date()
  const diff = Math.ceil((deadline - today) / (1000 * 60 * 60 * 24))
  return diff >= 0 ? `D-${diff}` : '마감됨'
}

function isDeadlinePassed(vote) {
  if (!vote.createdDate || !vote.durationDays) return false
  const created = new Date(vote.createdDate)
  const deadline = new Date(created)
  deadline.setDate(deadline.getDate() + vote.durationDays)
  return new Date() > deadline
}

onMounted(fetchVotes)
</script>

<style scoped>
.voting-list-container {
  max-width: 540px;
  margin: auto;
  padding: 0.9rem 1.3rem 1.2rem 1.3rem;
  font-family: 'Pretendard', sans-serif;
  position: relative;
}

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
  font-size: 1.17rem;
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

.vote-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
  width: 100%
}

.vote-card {
  padding: 1rem;
  border: 1px solid #e0e0e0;
  border-radius: 10px;
  background-color: #fff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.vote-card:hover {
  background-color: #f9f9ff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.vote-title {
  font-weight: 600;
  font-size: 1.05rem;
  margin-bottom: 0.3rem;
}

.vote-info {
  font-size: 0.9rem;
  color: #666;
}

.vote-status {
  margin-top: 0.5rem;
  font-size: 0.85rem;
  font-weight: 500;
  color: #2563eb;
}

.vote-status.closed {
  color: #999;
}

.empty {
  text-align: center;
  padding: 2rem;
  color: #999;
}
</style>
