<template>
  <div class="votes-container">
    <header class="page-header">
      <h1>🗳️ 투표 관리</h1>
      <p>프로젝트를 선택하여 투표를 확인하고 생성할 수 있습니다.</p>
    </header>

    <div class="controls-wrapper card">
      <div class="project-selector">
        <label for="project-select">프로젝트 선택:</label>
        <select id="project-select" v-model="selectedProjectId">
          <option :value="null" disabled>-- 프로젝트를 선택하세요 --</option>
          <option v-for="project in projects" :key="project.projectId" :value="project.projectId">
            {{ project.title }}
          </option>
        </select>
      </div>
      <button class="create-btn" @click="showCreateModal = true" :disabled="!selectedProjectId">
        새 투표 생성
      </button>
    </div>

    <div class="content-wrapper card">
      <div v-if="!selectedProjectId" class="empty-state">
        <p>먼저 프로젝트를 선택해주세요.</p>
      </div>
      <div v-else>
        <VotingList :key="selectedProjectId" :project-id="selectedProjectId" :show-header="false" ref="votingListRef" />
      </div>
    </div>

    <!-- 투표 생성 모달 -->
    <VoteCreateModal
      v-if="showCreateModal"
      :project-id="selectedProjectId"
      @close="showCreateModal = false"
      @created="handleVoteCreated"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from '@/utils/axiosInstance.js';
import VotingList from '@/components/dashboard/VotingList.vue';
import VoteCreateModal from '@/components/dashboard/VoteCreateModal.vue';

const projects = ref([]);
const selectedProjectId = ref(null);
const showCreateModal = ref(false);
const votingListRef = ref(null);

async function fetchProfessorProjects() {
  try {
    const { data: meData } = await axios.get('/auth/me');
    projects.value = meData.projectId || [];
  } catch (error) {
    console.error('교수 프로젝트 목록 로딩 실패:', error);
    alert('관리 중인 프로젝트 목록을 불러오는 데 실패했습니다.');
  }
}

function handleVoteCreated() {
  showCreateModal.value = false;
  if (votingListRef.value) {
    votingListRef.value.fetchVotes();
  }
}

onMounted(() => {
  fetchProfessorProjects();
});
</script>

<style scoped>
.votes-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: #f7f8fc;
  height: calc(100vh - 60px);
}

.page-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #e9ecef;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 16px;
  color: #6c757d;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.controls-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-selector label {
  font-weight: 600;
}

.project-selector select {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ced4da;
  min-width: 250px;
}

.create-btn {
  padding: 10px 20px;
  background-color: #4f46e5;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.create-btn:hover {
  background-color: #3745ae;
}

.create-btn:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
}

.content-wrapper {
  flex-grow: 1;
  overflow-y: auto;
  padding: 0;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #868e96;
  font-size: 16px;
}
</style>