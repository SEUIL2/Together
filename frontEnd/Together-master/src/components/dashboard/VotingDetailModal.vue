<template>
  <div class="modal-backdrop" @click.self="close">
    <div class="modal">
      <button class="close-btn" @click="close">✖</button>
      
      <div v-if="vote && vote.items" class="modal-content">
        <!-- 타이틀, 작성자, 마감 -->
        <div class="title-row">
          <span class="title">{{ vote.title }}</span>
          <span class="author" v-if="vote.userName">by {{ vote.userName }}</span>
          <span class="deadline" :class="{ closed: isDeadlinePassed }">
            {{ isDeadlinePassed ? '마감됨' : dDayText }}
          </span>
        </div>
        <hr />

        <!-- 항목 리스트 -->
        <div class="option-list">
          <button
            v-for="item in vote.items"
            :key="item.voteItemId"
            class="option-btn"
            :class="{
              selected: mySelectedId === item.voteItemId,
              closed: isDeadlinePassed
            }"
            :disabled="hasVoted || isDeadlinePassed"
            @click="selectItem(item.voteItemId)"
          >
            <span class="option-text">{{ item.options }}</span>
            <!-- 결과: 투표했거나 마감됐을 때만 표시 -->
            <span v-if="hasVoted || isDeadlinePassed" class="percent-row">
              <span class="bar-wrap">
                <span class="bar" :style="{ width: getPercent(item) + '%' }"></span>
              </span>
              <span class="percent">{{ getPercent(item) }}%</span>
              <span class="count">{{ item.responseCount }}표</span>
            </span>
            <span
              v-if="mySelectedId === item.voteItemId && (hasVoted || isDeadlinePassed)"
              class="my-mark"
              >✔️</span
            >
          </button>
        </div>

        <!-- 안내문구 -->
        <div class="info-row">
          <template v-if="isDeadlinePassed">
            <span class="closed-msg">이 투표는 마감되었습니다.</span>
          </template>
          <template v-else-if="hasVoted">
            <span class="done-msg">이미 투표하셨습니다. 결과가 공개됩니다.</span>
          </template>
          <template v-else>
            <span class="tip">항목을 선택하면 투표가 제출되고 결과가 공개됩니다.</span>
          </template>
        </div>
      </div>
      <div v-else class="loading">불러오는 중...</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import api from '@/api'
const props = defineProps({ voteId: Number })
const emit = defineEmits(['close'])

const vote = ref(null)
const mySelectedId = ref(null)
const hasVoted = computed(() => mySelectedId.value !== null)
const isDeadlinePassed = computed(() => {
  if (!vote.value?.createdDate || !vote.value?.durationDays) return false
  const created = new Date(vote.value.createdDate)
  const deadline = new Date(created)
  deadline.setDate(deadline.getDate() + (vote.value.durationDays ?? 0))
  return new Date() > deadline
})

const dDayText = computed(() => {
  if (!vote.value?.createdDate || !vote.value?.durationDays) return ''
  const created = new Date(vote.value.createdDate)
  const deadline = new Date(created)
  deadline.setDate(deadline.getDate() + (vote.value.durationDays ?? 0))
  const today = new Date()
  const diff = Math.ceil((deadline - today) / (1000 * 60 * 60 * 24))
  return diff >= 0 ? `D-${diff}` : '마감됨'
})

// userName 가져오기 (로그인 후 localStorage에 저장돼 있어야 함)
const myUserName = (localStorage.getItem('userName') || '').trim()

async function fetchVoteDetail() {
  vote.value = null
  mySelectedId.value = null
  try {
    const res = await api.get(`/votes/${props.voteId}/detail`, {
      headers: { Authorization: localStorage.getItem('authHeader') },
    })
    vote.value = res.data

    // 콘솔로 vote 응답 전체 구조 확인
    console.log('✅ 투표 상세 응답:', vote.value)
    console.log('내 userName(localStorage):', myUserName)

    // 작성자(userName)도 콘솔로 체크
    console.log('투표 작성자(vote.userName):', vote.value.userName)

    // 각 항목의 투표자 명단 콘솔 출력
    if (vote.value && Array.isArray(vote.value.items)) {
      vote.value.items.forEach(item => {
        console.log(`[${item.options}] 투표자:`, item.voterNames)
      })
      // 내 투표 찾기 (완전한 비교)
      const myItem = vote.value.items.find(item =>
        Array.isArray(item.voterNames) &&
        item.voterNames.some(name => (name || '').trim() === myUserName)
      )
      mySelectedId.value = myItem ? myItem.voteItemId : null
      console.log('내가 투표한 항목 id:', mySelectedId.value)
    }
  } catch (err) {
    console.error('❌ 투표 상세 정보 로딩 실패:', err)
    alert('투표 정보 로딩 실패')
    emit('close')
  }
}

function getTotalVotes() {
  if (!vote.value?.items) return 0
  return vote.value.items.reduce((sum, item) => sum + (item.responseCount || 0), 0)
}
function getPercent(item) {
  const total = getTotalVotes()
  if (total === 0) return 0
  return Math.round(((item.responseCount || 0) / total) * 100)
}

async function selectItem(voteItemId) {
  if (hasVoted.value || isDeadlinePassed.value) return
  try {
    await api.post(
      `/votes/${vote.value.voteId}/response`,
      { voteItemId },
      { headers: { Authorization: localStorage.getItem('authHeader') } }
    )
    await fetchVoteDetail()
  } catch (err) {
    alert('투표 실패 또는 중복 투표')
  }
}

function close() {
  emit('close')
}

onMounted(fetchVoteDetail)
watch(() => props.voteId, fetchVoteDetail)
</script>


<style scoped>
.modal-backdrop {
  position: fixed; z-index: 4000; inset: 0;
  background: rgba(32,32,40, 0.33);
  display: flex; align-items: center; justify-content: center;
}
.modal {
  min-width: 370px; max-width: 420px;
  background: #fff;
  border-radius: 17px;
  box-shadow: 0 6px 32px rgba(60,64,80,0.13);
  padding: 1.3rem 1.3rem 1.1rem 1.3rem;
  position: relative;
  animation: popup .2s;
}
@keyframes popup { from { scale:0.95; opacity:0.4;} to { scale:1; opacity:1;} }

.close-btn {
  position: absolute;
  top: 1.1rem; right: 1.12rem;
  background: none;
  border: none;
  font-size: 1.25rem;
  color: #444; cursor: pointer;
  z-index: 10;
}

.modal-content { margin-top: 0.7rem; }
.title-row {
  display: flex; align-items: flex-end; gap: 10px;
  justify-content: space-between;
  margin-bottom: 0.18rem;
  flex-wrap: wrap;
}
.title { font-size: 1.13rem; font-weight: 700; color: #23222b;}
.author { color: #666; font-size: 0.97rem; margin-left: 0.7em;}
.deadline {
  font-size: 0.95rem; color: #2563eb; font-weight: 600; margin-left: 0.5em;
}
.deadline.closed { color: #999; }

.option-list { margin: 1.13rem 0 0.85rem 0; display: flex; flex-direction: column; gap: 10px;}
.option-btn {
  display: flex; align-items: center; justify-content: space-between;
  background: #f7f8fe; border: 1.2px solid #d2d8f6;
  border-radius: 9px; padding: 0.83em 1.15em 0.83em 1.2em;
  font-size: 1.04rem; color: #23222b; cursor: pointer;
  font-weight: 500;
  transition: background 0.13s, border 0.12s;
  position: relative;
}
.option-btn.selected { background: #ecedfa; border-color: #7289f5; }
.option-btn.closed { cursor: not-allowed; opacity: 0.84; }
.option-btn:disabled { background: #f3f3f3; color: #aaa; cursor: not-allowed; }
.option-text { flex: 1; text-align: left; font-size: 1.08em;}

.percent-row {
  display: flex; align-items: center; gap: 8px;
  font-size: 0.96rem;
  margin-left: 1.05em;
}
.bar-wrap { background: #e0e5ff; height: 7px; border-radius: 5px; width: 61px; overflow: hidden;}
.bar { background: #4f46e5; height: 100%; border-radius: 5px; transition: width 0.2s;}
.percent { min-width: 2.2em; text-align: right; color: #2563eb;}
.count { color: #888; min-width: 2.1em; text-align: right;}

.my-mark {
  font-size: 1.1em; color: #17813c;
  margin-left: 6px;
}
.info-row { text-align: center; font-size: 0.99rem; color: #888; margin-top: 1.1rem; }
.tip { color: #6b79d4; }
.closed-msg { color: #d12f4d; }
.done-msg { color: #17813c; }
.loading { text-align: center; color: #888; padding: 1.5em 0;}
</style>
