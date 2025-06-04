<template>
  <div class="voting-list">
    <!-- 카드 헤더 -->
    <div class="card-header">
      <h3>투표</h3>
      <button class="btn-add" @click="showCreateModal = true">+</button>
    </div>

    <!-- 투표 리스트 -->
    <div class="vote-items">
      <div
        v-for="vote in votes"
        :key="vote.voteId"
        class="vote-item-card"
        @click="openModal(vote)">
        <h4 class="vote-title">{{ vote.title }}</h4>
<p class="vote-meta">
  {{ vote.userName }} · {{ formatDate(vote.createdDate) }}
</p>
      </div>
      <p v-if="votes.length === 0" class="empty">등록된 투표가 없습니다.</p>
    </div>

    <!-- 투표 응답 모달 -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h3>{{ selectedVote.title }}</h3>
        <ul class="modal-options">
          <li
            v-for="item in selectedVote.voteItems || []"
            :key="item.voteItemId"
            :class="{ selected: isMyVote(item.voteItemId) }"
            class="modal-option-item">
            <label>
              <input
                type="radio"
                v-model="selectedOption"
                :value="item.voteItemId"
                :disabled="hasVoted"
              />
              {{ item.options }}
              <span v-if="voteCounts[item.voteItemId] !== undefined">
                ({{ voteCounts[item.voteItemId] }}표)
              </span>
              <span v-if="isMyVote(item.voteItemId)">✔</span>
            </label>
          </li>
        </ul>
        <div class="modal-actions">
          <button
            class="btn-vote"
            @click="submitVote"
            :disabled="!selectedOption || hasVoted"
          >
            {{ hasVoted ? '투표 완료' : '투표하기' }}
          </button>
          <button class="btn-close" @click="closeModal">닫기</button>
        </div>
        <p v-if="hasVoted" class="already-voted-msg">이미 투표하셨습니다.</p>
      </div>
    </div>

    <!-- 새 투표 생성 모달 -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="closeCreateModal">
      <div class="modal-content">
        <h3>새 투표 생성</h3>
        <VoteCreate @created="onCreated" />
        <div class="modal-actions">
          <button class="btn-close" @click="closeCreateModal">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import axios from 'axios'
import VoteCreate from './VoteCreate.vue'
import { nextTick } from 'vue'

const votes = ref([])
const currentUserId = ref(null)
const showModal = ref(false)
const showCreateModal = ref(false)
const selectedVote = ref(null)
const selectedOption = ref(null)
const voteCounts = ref({})

const props = defineProps({
  projectId: Number,
});
async function fetchUser() {
  try {
    const res = await axios.get('/auth/me', { withCredentials: true });
    console.log('📝 /auth/me 응답 →', res.data);
    // data 구조를 정확히 보고, 숫자 ID만 뽑아내세요
    // 예: res.data.user.userId 여기에 실제 숫자가 있는지 확인
    currentUserId.value = Number(res.data.user?.userId);
    console.log('📝 currentUserId →', currentUserId.value);
  } catch (err) {
    console.error('fetchUser 실패:', err);
  }
}


async function fetchVotes() {
  try {
    const res = await axios.get(`/votes/project?projectId=${props.projectId}`, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    votes.value = res.data || [];
  } catch (e) {
    console.error('투표 목록 로드 실패:', e);
    votes.value = [];
  }
}


function onCreated() {
  closeCreateModal()
  fetchVotes()
}

async function openModal(vote) {
  try {
    // 1) 서버에서 Detail API로 응답 수 포함된 데이터를 가져옵니다
    const { data: detail } = await axios.get(
      `/votes/${vote.voteId}/detail`,
      {
        headers:      { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      }
    );

    // 2) selectedVote와 voteCounts 세팅
    selectedVote.value = {
      voteId: detail.voteId,
      title:  detail.title,
      voteItems: detail.items.map(i => ({
        voteItemId: i.voteItemId,
        options:    i.options
      })),
      // hasVoted 계산을 detail.items 기반으로 바꿔도 좋습니다
      voteResponseEntitys: []  
    };
    voteCounts.value = detail.items.reduce((acc, i) => {
      acc[i.voteItemId] = i.responseCount;
      return acc;
    }, {});

    // 3) 모든 데이터가 준비된 이후에 모달 열기
    showModal.value = true;
  } catch (e) {
    console.error('투표 상세 정보 불러오기 실패:', e);
  }
}



function closeModal() {
  showModal.value = false
  selectedVote.value = null
  selectedOption.value = null
  voteCounts.value = {}
}

function closeCreateModal() {
  showCreateModal.value = false
}

const hasVoted = computed(() =>
  selectedVote.value?.voteResponseEntitys?.some(
    r => r.user?.userId === currentUserId.value
  )
)

function isMyVote(voteItemId) {
  return selectedVote.value?.voteResponseEntitys?.some(
    r => r.user?.userId === currentUserId.value &&
         r.voteItem?.voteItemId === voteItemId
  )
}

function computeCounts() {
  const counts = {}
  const responses = selectedVote.value?.voteResponseEntitys || []
  const items = selectedVote.value?.voteItems || []

  items.forEach(item => {
    counts[item.voteItemId] = responses.filter(
      r => String(r.voteItem?.voteItemId) === String(item.voteItemId)
    ).length
  })

  voteCounts.value = counts

  console.log('[🧮 voteCounts 계산 결과]', voteCounts.value)
}



async function submitVote() {
  if (!selectedOption.value || hasVoted.value) return;
  try {
    // 1) 투표 등록
    await axios.post(
      `/votes/${selectedVote.value.voteId}/response`,
      { voteItemId: selectedOption.value },
      { headers:{ Authorization: localStorage.getItem('authHeader') }, withCredentials:true }
    );

    // 2) 서버에서 계산된 counts 한번에 받아오기
    const { data: detail } = await axios.get(
      `/votes/${selectedVote.value.voteId}/detail`,
      { headers:{ Authorization: localStorage.getItem('authHeader') }, withCredentials:true }
    );
    // detail.items는 List<VoteItemResultDTO> 형태:
    // [{ voteItemId, options, responseCount }, …]

    // 3) voteCounts 갱신
    const counts = {};
    detail.items.forEach(item => {
      counts[item.voteItemId] = item.responseCount;
    });
    voteCounts.value = counts;

    // (선택) 제목이나 다른 UI 업데이트가 필요하면…
    selectedVote.value.title = detail.title; 
    // —> 하지만 voteItems 배열은 기존 엔티티 그대로 써도 무방합니다.

  } catch (e) {
    console.error('투표 제출 실패:', e.response?.status, e.response?.data);
  }
}
function formatDate(date) {
  const d = new Date(date)
  return d.toLocaleDateString('ko-KR', {
    year: 'numeric', month: '2-digit', day: '2-digit'
  })
}

onMounted(async () => {
  await fetchUser()
  await fetchVotes()
})
</script>

<style scoped>
.voting-list { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; padding-bottom: 8px; border-bottom: 1px solid #eee; }
.card-header h3 { margin: 0; font-size: 20px; }
.btn-add { background: none; border: none; font-size: 24px; cursor: pointer; }
.vote-items { display: flex; flex-direction: column; gap: 12px; margin-top: 8px; }
.vote-item-card { padding: 12px; border: 1px solid #ddd; border-radius: 8px; cursor: pointer; }
.vote-title { margin: 0 0 4px; font-size: 16px; }
.vote-meta { margin: 0; font-size: 12px; color: #777; }
.empty { text-align: center; color: #777; margin-top: 16px; }

.modal-overlay { position: fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); display:flex; justify-content:center; align-items:center; z-index:1000; }
.modal-content { background:white; border-radius:8px; padding:24px; width:90%; max-width:400px; }
.modal-options { list-style:none; padding:0; margin:16px 0; }
.modal-option-item { margin-bottom:12px; }
.modal-option-item.selected { background-color: #e6f7ff; border-radius: 4px; padding: 4px; }
.modal-actions { display:flex; justify-content:space-between; gap:12px; margin-top:16px; }
.btn-vote { flex:1; padding:8px 16px; border:none; border-radius:6px; background:#3f8efc; color:#fff; cursor:pointer; }
.btn-vote:disabled { background:#ccc; cursor:default; }
.btn-close { flex:1; padding:8px 16px; border:none; border-radius:6px; background:#eee; cursor:pointer; }
.already-voted-msg { color: red; font-weight: bold; margin-top: 10px; }
</style>
