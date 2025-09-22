<template>
  <div class="team-card">
    <!-- 프로젝트 정보 -->
    <div class="card-header">
      <div class="project-info">
        <h3 class="project-title">{{ project.title }}</h3>
        <p class="project-desc">{{ project.description || '프로젝트 소개가 없습니다.' }}</p>
      </div>
      <button class="view-button" @click="$emit('viewProject', project.projectId)">
        <i class="arrow">➤</i>
      </button>
    </div>

    <!-- 진행도 -->
    <div class="progress-wrapper">
      <div class="progress-label-text">
        📊 프로젝트 진행도 <strong>{{ project.progress ?? 0 }}%</strong>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: `${project.progress ?? 0}%` }"></div>
      </div>
    </div>

    <!-- 팀원 -->
    <div class="team-members">
      <div
          v-for="(member, index) in studentMembers"
          :key="index"
          class="avatar"
      >
        <div class="avatar-img">
          <img :src="member.profileImageUrl || defaultImage" />
        </div>
        <span class="member-name">{{ member.userName }}</span>
      </div>
    </div>

    <!-- 버튼들 -->
    <div class="action-buttons">
      <button class="action-btn" @click="showNoticeModal = true">📢 공지사항</button>
      <button class="action-btn" @click="showVoteModal = true">🗳 투표</button>

      <button class="action-btn" @click="showFeedbackModal = true">📝 피드백 내역</button>
      <button class="action-btn" @click="showMemoModal = true">🧾 메모</button>
    </div>

    <!-- 공지사항 모달 -->
    <NoticeModal
        v-if="showNoticeModal"
        :projectId="project.projectId"
        @close="showNoticeModal = false"
    />

    <!-- 피드백 모달 -->
    <FeedbackHistoryModal
        v-if="showFeedbackModal"
        :projectId="project.projectId"
        @close="showFeedbackModal = false"
    />

    <ProjectMemoModal
        v-if="showMemoModal"
        :projectId="project.projectId"
        @close="showMemoModal = false"
    />
    <!-- 투표 모달 -->
    <VotingListModalWrapper
        v-if="showVoteModal"
        :projectId="project.projectId"
        @close="showVoteModal = false"
    />



  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import NoticeModal from '@/components/notice/NoticeModal.vue'
import FeedbackHistoryModal from '@/components/feedback/FeedbackHistoryModal.vue'
import VotingListModalWrapper from '@/components/dashboard/VotingListModalWrapper.vue'
import ProjectMemoModal from '@/components/professor/ProjectMemoModal.vue'
const props = defineProps({ project: Object })

const showNoticeModal = ref(false)
const showFeedbackModal = ref(false)
const showVoteModal = ref(false)
const showMemoModal = ref(false)

onMounted(() => {
  console.log('TeamCard가 받은 project 데이터:', JSON.parse(JSON.stringify(props.project)));
});

const defaultImage = new URL('@/assets/defaultimage.png', import.meta.url).href;
const studentMembers = computed(() => {
  return (props.project?.members || []).filter(m => m.role === 'STUDENT');
});

</script>



<style scoped>
.team-card {
  width: 550px;
  background-color: #ffffff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: box-shadow 0.3s ease;
}
.team-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.project-info {
  max-width: 85%;
}

.project-title {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
  color: #1e1e1e;
}

.project-desc {
  font-size: 14px;
  color: #555;
  line-height: 1.4;
  overflow-wrap: break-word;
}

.view-button {
  background-color: transparent;
  border: none;
  font-size: 20px;
  cursor: pointer;
  color: #888;
  transition: transform 0.2s ease;
}
.view-button:hover {
  transform: translateX(3px);
  color: #111;
}

.progress-wrapper {
  margin: 18px 0 10px;
}

.progress-label-text {
  font-size: 14px;
  font-weight: 500;
  color: #444;
  margin-bottom: 6px;
}

.progress-bar {
  width: 100%;
  height: 10px;
  background-color: #e9edf3;
  border-radius: 6px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background-color: #4a90e2;
  border-radius: 6px;
  transition: width 0.4s ease;
}

.team-members {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 12px 0 18px;
}
.avatar {
  display: flex;
  align-items: center;
  gap: 6px;
  background-color: #f9f9f9;
  border: 1px solid #ddd;
  border-radius: 18px;
  padding: 5px 12px;
}
.avatar-img img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}
.member-name {
  font-size: 13px;
  color: #333;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}
.action-btn {
  flex: 1 1 45%;
  background-color: #4a4a4a;
  color: #fff;
  padding: 10px 14px;
  font-size: 13px;
  border-radius: 14px;
  border: none;
  cursor: pointer;
  transition: background-color 0.25s ease;
}
.action-btn:hover {
  background-color: #222;
}
</style>
