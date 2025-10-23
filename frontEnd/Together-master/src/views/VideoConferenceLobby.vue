<template>
  <div class="lobby-container">
    <div class="lobby-card">
      <div class="card-header">
        <h1 class="card-title">화상회의</h1>
        <p class="card-subtitle">프로젝트 팀원들과 실시간으로 소통하세요.</p>
      </div>
      <div class="card-body">
        <div class="project-info">
          <p><strong>프로젝트:</strong> {{ projectTitle || '정보 없음' }}</p>
        </div>
        <button class="start-button" @click="startConference">
          회의 시작하기
        </button>
      </div>
      <div class="card-footer">
        <p>카메라와 마이크가 준비되었는지 확인해주세요.</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const projectId = ref(null);
const projectTitle = ref('');
const isReadOnly = ref(false);

onMounted(() => {
  projectId.value = route.query.projectId;
  projectTitle.value = route.query.projectTitle;
  isReadOnly.value = route.query.readonly === 'true';

  if (!projectId.value) {
    alert('프로젝트 정보가 올바르지 않습니다. 대시보드로 돌아갑니다.');
    router.push('/DashBoard');
  }
});

const startConference = () => {
  const videoQuery = {
    channel: projectId.value,
    projectId: projectId.value, // VideoChat.vue에서 projectId를 사용할 수 있도록 추가
    projectTitle: projectTitle.value,
    noLayout: 'true' // 레이아웃을 숨기기 위한 쿼리 파라미터 추가
  };

  if (isReadOnly.value) {
    videoQuery.readonly = 'true';
  }

  // 새 탭에서 화상회의 페이지를 엽니다.
  const routeData = router.resolve({
    name: 'VideoChat',
    query: videoQuery,
  });
  window.open(routeData.href, '_blank');
};
</script>

<style scoped>
.lobby-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: calc(100vh - 60px);
  background-color: #f8f9fa;
}

.lobby-card {
  width: 100%;
  max-width: 500px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  padding: 40px;
  text-align: center;
}

.card-header {
  margin-bottom: 32px;
}

.card-title {
  font-size: 28px;
  font-weight: 700;
  color: #343a40;
  margin: 0 0 8px;
}

.card-subtitle {
  font-size: 16px;
  color: #868e96;
  margin: 0;
}

.project-info {
  margin-bottom: 32px;
  font-size: 16px;
  color: #495057;
}

.start-button {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
  background-color: #3f8efc;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.start-button:hover {
  background-color: #3578e5;
}

.card-footer {
  margin-top: 24px;
  font-size: 14px;
  color: #adb5bd;
}
</style>