<template>
  <div class="voting-card">
    <h3>{{ vote.title }}</h3>

    <ul class="options-list">
      <li
        v-for="item in vote.voteItems"
        :key="item.voteItemId"
        :class="{ selected: isMyVote(item.voteItemId) }"
      >
        <label>
          <input
            type="radio"
            :value="item.voteItemId"
            v-model="selectedOption"
            :disabled="hasVoted"
          />
          {{ item.options }}
          <span v-if="vote.voteResponseEntitys">
            ({{ countVotes(item.voteItemId) }}표)
          </span>
          <span v-if="isMyVote(item.voteItemId)">✔</span>
        </label>
      </li>
    </ul>

    <button
      class="vote-btn"
      @click="submitVote"
      :disabled="!selectedOption || hasVoted"
    >
      {{ hasVoted ? '투표 완료' : '투표하기' }}
    </button>

    <p v-if="hasVoted" class="already-voted-msg">
      이미 투표하셨습니다.
    </p>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import axios from 'axios'

const props = defineProps({
  vote: { type: Object, required: true },
  currentUserId: { type: [String, Number], required: true }
})
const emit = defineEmits(['voted'])

const selectedOption = ref(null)

// ✅ 이미 투표했는지 확인
const hasVoted = computed(() => {
  return props.vote.voteResponseEntitys?.some(
    r => r.user?.userId === props.currentUserId
  )
})

// ✅ 해당 항목의 투표 수 계산
function countVotes(voteItemId) {
  return props.vote.voteResponseEntitys
    ? props.vote.voteResponseEntitys.filter(r => r.voteItem.voteItemId === voteItemId).length
    : 0
}

// ✅ 현재 사용자가 선택한 항목인지 확인
function isMyVote(voteItemId) {
  return props.vote.voteResponseEntitys?.some(
    r => r.user?.userId === props.currentUserId && r.voteItem.voteItemId === voteItemId
  )
}

// ✅ 투표 전송
async function submitVote() {
  if (!selectedOption.value) return
  try {
    await axios.post(
      `/votes/${props.vote.voteId}/response`,
      {
        voteItemId: selectedOption.value
      },
      { headers: { Authorization: localStorage.getItem('authHeader') }, withCredentials: true }
    )
    emit('voted') // 부모 컴포넌트에서 다시 불러오게
  } catch (error) {
    alert('투표 중 오류가 발생했습니다.')
    console.error(error)
  }
}
</script>

<style scoped>
.voting-card {
  padding: 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
.options-list {
  list-style: none;
  padding: 0;
  margin-bottom: 12px;
}
.options-list li {
  margin-bottom: 6px;
  padding: 4px;
}
.vote-btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  background: #3f8efc;
  color: #fff;
  cursor: pointer;
}
.vote-btn:disabled {
  background: #ccc;
  cursor: default;
}
.selected {
  background-color: #e6f2ff;
  border-radius: 4px;
}
.already-voted-msg {
  color: red;
  font-weight: bold;
  margin-top: 10px;
}
</style>
