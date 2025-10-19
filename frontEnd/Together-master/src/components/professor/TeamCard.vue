<template>
  <div class="team-card">
    <!-- 프로젝트 정보 -->
    <div class="card-header">
      <div class="project-info">
        <h3 class="project-title">{{ project.title }}</h3>
        <p class="project-desc">{{ project.description || '프로젝트 소개가 없습니다.' }}</p>
      </div>
      <button class="view-button" @click="$emit('viewProject', project.projectId)">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
          <polyline points="9 18 15 12 9 6"></polyline>
        </svg>
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
      <button class="action-btn" @click="$emit('createFeedback', project.projectId)">📝 피드백 생성</button>
      <button class="action-btn" @click="showMemoModal = true">🧾 메모</button>
      <button class="action-btn" @click="goToVideoChat">📹 화상채팅</button>
      <button class="action-btn" @click="goToReport">📄 보고서</button>
    </div>

    <!-- 공지사항 모달 -->
    <NoticeModal
        v-if="showNoticeModal"
        :projectId="project.projectId"
        @close="showNoticeModal = false"
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
import { useRouter } from 'vue-router'
import NoticeModal from '@/components/notice/NoticeModal.vue'
import FeedbackHistoryModal from '@/components/feedback/FeedbackHistoryModal.vue'
import VotingListModalWrapper from '@/components/dashboard/VotingListModalWrapper.vue'
import ProjectMemoModal from '@/components/professor/ProjectMemoModal.vue'

const props = defineProps({ project: Object })
const router = useRouter()

const showNoticeModal = ref(false)
const showVoteModal = ref(false)
const showMemoModal = ref(false)

onMounted(() => {
  console.log('TeamCard가 받은 project 데이터:', JSON.parse(JSON.stringify(props.project)));
});

const defaultImage = new URL('@/assets/defaultimage.png', import.meta.url).href;
const studentMembers = computed(() => {
  return (props.project?.members || []).filter(m => m.role === 'STUDENT');
});

// 화상채팅으로 이동 (교수 열람 모드 유지)
const goToVideoChat = () => {
  if (!props.project?.projectId) {
    alert('프로젝트 정보가 없습니다.');
    return;
  }
  const videoQuery = {
    channel: String(props.project.projectId),
    readonly: 'true'
  };
  router.push({ name: 'VideoChat', query: videoQuery });
};

// 보고서로 이동 (교수 열람 모드 유지)
const goToReport = () => {
  if (!props.project?.projectId) {
    alert('프로젝트 정보가 없습니다.');
    return;
  }
  router.push({
    path: '/professor/report',
    query: {
      projectId: props.project.projectId,
      readonly: 'true'
    }
  });
};

</script>



<style scoped>
.team-card {
  width: 100%;
  max-width: 550px;
  background-color: #ffffff;
  border-radius: 20px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  transition: box-shadow 0.3s ease, transform 0.3s ease;
}
.team-card:hover {
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.08);
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-info {
  max-width: 85%;
}

.project-title {
  font-size: 22px;
  font-weight: 800;
  margin-bottom: 6px;
  color: #2c3e50;
}

.project-desc {
  font-size: 14px;
  color: #64748b;
  line-height: 1.4;
  overflow-wrap: break-word;
  /* 긴 설명은 2줄로 제한하고 말줄임표 표시 */
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.view-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  background-color: #f1f5f9;
  border: none;
  border-radius: 50%;
  cursor: pointer;
  color: #64748b;
  transition: all 0.2s ease;
}
.view-button:hover {
  background-color: #e2e8f0;
  color: #2c3e50;
}

.progress-wrapper {
  margin: 18px 0 10px;
}

.progress-label-text {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
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
  background: linear-gradient(90deg, #60a5fa, #3b82f6);
  border-radius: 6px;
  transition: width 0.4s ease;
}

.team-members {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 16px 0 24px;
}
.avatar {
  display: flex;
  align-items: center;
  gap: 6px;
}
.avatar-img img {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #e2e8f0;
}
.member-name {
  font-size: 13px;
  color: #475569;
  font-weight: 500;
}

.action-buttons {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}
.action-btn {
  flex: 1 1 45%;
  background-color: #f1f5f9;
  color: #475569;
  padding: 10px 14px;
  font-size: 13px;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: background-color 0.25s ease;
}
.action-btn:hover {
  background-color: #e2e8f0;
}
</style>
