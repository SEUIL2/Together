<template>
  <div class="voting-list-container">
    <!-- 헤더 -->
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
const projectId = Number(localStorage.getItem('currentProjectId'))

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
  selectedVoteId.value = id
}

function isDeadlinePassed(vote) {
  if (!vote.deadLine) return false
  return new Date() > new Date(vote.deadLine)
}

function getDDay(vote) {
  if (!vote.deadLine) return ''
  const deadline = new Date(vote.deadLine)
  const today = new Date()
  const diff = Math.ceil((deadline - today) / (1000 * 60 * 60 * 24))
  return diff >= 0 ? `D-${diff}` : '마감됨'
}

function formatDate(dateStr) {
  const date = new Date(dateStr)
  return `${date.getFullYear()}.${String(date.getMonth() + 1).padStart(2, '0')}.${String(date.getDate()).padStart(2, '0')}`
}

onMounted(fetchVotes)
</script>

<style scoped>
.voting-list-container {
  max-width: 580px;
  margin: auto;
  padding: 1rem 1.3rem 1.2rem 1.3rem;
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
.create-button:hover {
  background: #3745ae;
}

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
.close-btn:hover {
  background: #f0f0f6;
  color: #e23333;
}

.vote-cards {
  display: flex;
  flex-direction: column;
  gap: 14px;
  width: 100%;
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
}

.vote-dday {
  font-size: 0.82rem;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: 12px;
  color: #3b49df;
  background-color: #edf0ff;
}
.vote-dday.closed {
  color: #aaa;
  background-color: #f2f2f2;
}

.vote-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 0.93rem;
  color: #555;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.meta-icon {
  font-size: 0.95rem;
}

.empty {
  text-align: center;
  padding: 2rem;
  color: #999;
}
</style>
