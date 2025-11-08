<template>
  <div class="lobby-background">
    <div class="lobby-container">
      <div class="lobby-header">
        <h1 class="project-title">{{ projectTitle }}</h1>
        <p class="project-subtitle">화상회의 로비</p>
      </div>

      <div class="participants-section">
        <h2 class="section-title">현재 접속 중인 팀원 ({{ connectedUsers.length }}명)</h2>
        <div v-if="isLoading" class="loading-spinner">
          <div class="spinner"></div>
          <p>참여자 정보를 불러오는 중...</p>
        </div>
        <ul v-else-if="connectedUsers.length > 0" class="participants-list">
          <li v-for="user in connectedUsers" :key="user.userId" class="participant-item">
            <span class="avatar">👤</span>
            <span class="username">{{ user.userName }}</span>
          </li>
        </ul>
        <div v-else class="no-participants">
          <p>아직 참여한 팀원이 없습니다.</p>
        </div>
      </div>

      <div class="lobby-actions">
        <button class="join-btn" @click="joinChannel">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4"/><polyline points="10 17 15 12 10 7"/><line x1="15" y1="12" x2="3" y2="12"/></svg>
          <span>참여하기</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import api from '@/api';

const route = useRoute();
const router = useRouter();

const connectedUsers = ref([]);
const isLoading = ref(true);
let pollingInterval = null;
const memberInfoMap = ref(new Map());

const projectId = computed(() => route.query.projectId);
const projectTitle = computed(() => route.query.projectTitle || '프로젝트');

const fetchProjectMembers = async () => {
  if (!projectId.value) return;
  try {
    const response = await api.get('/projects/members', { params: { projectId: projectId.value } });
    const members = response.data || [];
    const newMap = new Map();
    members.forEach(member => newMap.set(member.userId, member.userName));
    memberInfoMap.value = newMap;
  } catch (error) {
    console.error('프로젝트 멤버 정보를 가져오는 데 실패했습니다.', error);
  }
};

const fetchConnectedUsers = async () => {
  if (!projectId.value) return;
  try {
    const response = await api.get('/agora/participants', {
      params: { projectId: projectId.value }
    });
    const participantData = response.data.data || {};
    const userIds = participantData.users || [];

    connectedUsers.value = userIds.map(userId => ({
      userId,
      userName: memberInfoMap.value.get(userId) || `사용자 ${userId}`
    }));
  } catch (error) {
    console.error('접속 중인 사용자 정보를 가져오는 데 실패했습니다.', error);
    connectedUsers.value = []; // 에러 발생 시 목록 비우기
  } finally {
    isLoading.value = false;
  }
};

const joinChannel = () => {
  router.push({
    name: 'VideoChat',
    query: {
      ...route.query,
      channel: projectId.value, // 채널 ID로 projectId 사용
    },
  });
};

onMounted(async () => {
  // API 요청 전에 인증 헤더를 명시적으로 설정하여 403 오류를 방지합니다.
  const authHeader = localStorage.getItem('authHeader');
  if (authHeader) {
    api.defaults.headers.common['Authorization'] = authHeader;
  }

  await fetchProjectMembers(); // 멤버 정보를 먼저 가져옵니다.
  await fetchConnectedUsers(); // 그 다음 접속자 정보를 가져옵니다.
  // 5초마다 현재 접속자 정보를 폴링
  pollingInterval = setInterval(fetchConnectedUsers, 5000);
});

onUnmounted(() => {
  // 컴포넌트가 파괴될 때 폴링 중지
  if (pollingInterval) {
    clearInterval(pollingInterval);
  }
});
</script>

<style scoped>
.lobby-background {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}

.lobby-container {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 16px; /* 모서리 둥글기 */
  padding: 32px 40px; /* 상하 패딩을 줄여 전체 높이 조정 */
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.lobby-header {
  margin-bottom: 32px;
}

.project-title {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin: 0;
}

.project-subtitle {
  font-size: 16px;
  color: #6b7280;
  margin-top: 8px;
}

.participants-section {
  margin-bottom: 32px;
  min-height: 150px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 16px;
}

.loading-spinner, .no-participants {
  padding: 24px 0;
  color: #6b7280;
}

.spinner {
  margin: 0 auto 16px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3b82f6;
  border-radius: 50%;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.participants-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.participant-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background-color: #f3f4f6;
  padding: 10px;
  border-radius: 8px;
}

.avatar {
  font-size: 18px;
}

.username {
  font-size: 15px;
  font-weight: 500;
  color: #111827;
}

.lobby-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.join-btn {
  padding: 14px 20px;
  border-radius: 8px;
  border: none;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.join-btn {
  background-color: #3b82f6;
  color: white;
}
.join-btn:hover {
  background-color: #2563eb;
}
</style>