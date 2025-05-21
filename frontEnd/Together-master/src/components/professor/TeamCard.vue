<template>
  <div class="team-card">
    <h3>{{ project.title }}</h3>
    <p>{{ project.description }}</p>
    <p>진행도: {{ project.progress }}%</p>

    <!-- 팀원 버튼 -->
    <div class="team-members">
      <button
        v-for="member in project.members"
        :key="member.id"
        @click="openMemberEvaluation(member)"
      >
        {{ member.name }}
      </button>
    </div>

    <!-- 카드 버튼 -->
    <div class="card-buttons">
      <button @click="openNotice">공지사항 작성</button>
      <button @click="openVote">투표 작성</button>
      <button @click="openFeedback">피드백 내역</button>
      <button @click="openMemo">팀 메모</button>
      <button @click="viewProject">프로젝트 보기</button>
    </div>

    <!-- 모달들 -->
    <TeamMemberEvaluationModal
      v-if="showEvaluation"
      :member="selectedMember"
      @close="showEvaluation = false"
    />
    <VoteCreate
      v-if="showVote"
      :project-id="project.projectId"
      @close="showVote = false"
    />
    <NoticeCreate
      v-if="showNotice"
      :project-id="project.projectId"
      @close="showNotice = false"
    />
    <TeamFeedbackListModal
      v-if="showFeedback"
      :teamId="project.projectId"
      @close="showFeedback = false"
    />
    <TeamMemoModal
      v-if="showMemo"
      :teamId="project.projectId"
      @close="showMemo = false"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import TeamMemberEvaluationModal from './TeamMemberEvaluationModal.vue'
import VoteCreate from '@/components/dashboard/VoteCreate.vue'
import NoticeCreate from '@/components/dashboard/DashBoardNotice.vue'
import TeamFeedbackListModal from './TeamFeedbackListModal.vue'
import TeamMemoModal from './TeamMemoModal.vue'

// ✅ props 이름 수정: project
const props = defineProps({ project: Object })
const emit = defineEmits(['view-project'])

const showEvaluation = ref(false)
const showNotice = ref(false)
const showVote = ref(false)
const showFeedback = ref(false)
const showMemo = ref(false)

const selectedMember = ref(null)

function openMemberEvaluation(member) {
  selectedMember.value = member
  showEvaluation.value = true
}

function openNotice() {
  showNotice.value = true
}

function openVote() {
  showVote.value = true
}

function openFeedback() {
  showFeedback.value = true
}

function openMemo() {
  showMemo.value = true
}

function viewProject() {
  emit('view-project', props.project.projectId, props.project.title)
}
</script>

<style scoped>
.team-card {
  border: 1px solid #ddd;
  padding: 16px;
  border-radius: 10px;
  background-color: #fff;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.05);
}

.team-members {
  margin-top: 10px;
}

.team-members button {
  margin-right: 8px;
  margin-bottom: 6px;
  padding: 6px 10px;
  border: none;
  background-color: #e0f0ff;
  color: #333;
  border-radius: 4px;
  cursor: pointer;
}

.card-buttons {
  margin-top: 16px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.card-buttons button {
  padding: 8px 12px;
  background-color: #3f8efc;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.card-buttons button:hover {
  background-color: #2f6edc;
}
</style>
