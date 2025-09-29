<template>
  <div class="dashboard-container">
    <div class="environment-setup-card">
      <div class="card-header">
        <div class="card-title-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-sliders">
            <line x1="4" y1="21" x2="4" y2="14"></line>
            <line x1="4" y1="10" x2="4" y2="3"></line>
            <line x1="12" y1="21" x2="12" y2="12"></line>
            <line x1="12" y1="8" x2="12" y2="3"></line>
            <line x1="20" y1="21" x2="20" y2="16"></line>
            <line x1="20" y1="12" x2="20" y2="3"></line>
            <line x1="1" y1="14" x2="7" y2="14"></line>
            <line x1="9" y1="8" x2="15" y2="8"></line>
            <line x1="17" y1="16" x2="23" y2="16"></line>
          </svg>
          <h3 class="board-title">프로젝트 개발 환경 설정</h3>
        </div>
        <p class="card-subtitle">프로젝트의 기술 스택과 개발 환경을 설정해주세요.</p>
      </div>
      <div class="card-body">
        <form @submit.prevent="saveEnvironment" class="environment-form">
          <div class="form-row">
            <div class="form-group">
              <label for="os">운영체제 (OS)</label>
              <input id="os" type="text" v-model="environment.operatingSystem" placeholder="예: Windows 11, macOS Sonoma">
            </div>
            <div class="form-group">
              <label for="ide">통합 개발 환경 (IDE)</label>
              <input id="ide" type="text" v-model="environment.ide" placeholder="예: IntelliJ IDEA, VS Code">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="lang">개발 언어</label>
              <input id="lang" type="text" v-model="environment.devLanguage" placeholder="예: Java 17, JavaScript ES6">
            </div>
            <div class="form-group">
              <label for="framework">프레임워크</label>
              <input id="framework" type="text" v-model="environment.framework" placeholder="예: Spring Boot 3.1, Vue.js 3">
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label for="version-control">버전 관리 시스템</label>
              <input id="version-control" type="text" v-model="environment.versionControl" placeholder="예: Git, SVN">
            </div>
            <div class="form-group">
              <label for="database">데이터베이스</label>
              <input id="database" type="text" v-model="environment.database" placeholder="예: MySQL 8.0, PostgreSQL 15">
            </div>
          </div>
          <div class="form-group full-width">
            <label for="etc">기타</label>
            <textarea id="etc" v-model="environment.etc" placeholder="기타 필요한 라이브러리, 도구 등"></textarea>
          </div>
          <div class="form-actions">
            <button type="submit" class="save-btn">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="feather feather-save">
                <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"></path>
                <polyline points="17 21 17 13 7 13 7 21"></polyline>
                <polyline points="7 3 7 8 15 8"></polyline>
              </svg>
              <span>저장하기</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRoute } from 'vue-router';

const route = useRoute();
const projectId = ref(null);
const environment = ref({
  id: null,
  operatingSystem: '',
  ide: '',
  devLanguage: '',
  framework: '',
  versionControl: '',
  database: '',
  etc: ''
});

const fetchEnvironment = async () => {
  if (!projectId.value) return;
  try {
    const response = await axios.get('/api/dev-env', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    // 프로젝트에 설정된 첫 번째 환경 정보를 불러옴
    if (response.data && response.data.length > 0) {
      environment.value = response.data[0];
    }
  } catch (error) {
    console.error('개발 환경 정보를 불러오는 데 실패했습니다.', error);
  }
};

const saveEnvironment = async () => {
  if (!projectId.value) {
    alert('프로젝트 ID가 유효하지 않습니다.');
    return;
  }

  const requestDto = { ...environment.value };

  try {
    let response;
    // 기존 환경 정보가 있으면(id가 존재하면) 수정(PUT), 없으면 생성(POST)
    if (environment.value.id) {
      response = await axios.put(`/api/dev-env/${environment.value.id}`, requestDto, {
        headers: { Authorization: localStorage.getItem('authHeader') }
      });
    } else {
      response = await axios.post('/api/dev-env', requestDto, {
        headers: { Authorization: localStorage.getItem('authHeader') }
      });
      // 새로 생성된 경우, 반환된 id를 받아와 상태에 업데이트
      environment.value.id = response.data;
    }
    alert('개발 환경 정보가 성공적으로 저장되었습니다.');
    await fetchEnvironment(); // 저장 후 최신 정보 다시 불러오기
  } catch (error) {
    console.error('저장에 실패했습니다.', error);
    alert('개발 환경 정보 저장 중 오류가 발생했습니다.');
  }
};

onMounted(async () => {
  // 현재 사용자의 프로젝트 ID를 가져옴
  const { data: me } = await axios.get('/auth/me', { withCredentials: true });
  projectId.value = route.params.projectId || me.projectId;
  if(projectId.value) {
    await fetchEnvironment();
  }
});
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;500;700&display=swap');

.dashboard-container {
  padding: 24px;
  background-color: #f7f8fc;
  font-family: 'Noto Sans KR', sans-serif;
}

.environment-setup-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  /* 마우스 호버 시의 전환 효과를 제거했습니다. */
}

.card-header {
  margin-bottom: 24px;
  border-bottom: 1px solid #e9ecef;
  padding-bottom: 24px;
}

.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #2c3e50;
}

.card-title-wrapper svg {
  color: #3498db;
}

.board-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0;
}

.card-subtitle {
  font-size: 16px;
  color: #7f8c8d;
  margin-top: 8px;
}

.environment-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #34495e;
}

.form-group input,
.form-group textarea {
  padding: 14px 18px;
  border: 1px solid #dcdfe6;
  border-radius: 10px;
  font-size: 16px;
  background-color: #fdfdfe;
  transition: all 0.3s ease;
  color: #333;
}

.form-group input::placeholder,
.form-group textarea::placeholder {
  color: #adb5bd;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3498db;
  box-shadow: 0 0 0 3px rgba(52, 152, 219, 0.2);
  background-color: #fff;
}

.form-group textarea {
  min-height: 120px;
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.save-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 28px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, #3498db, #2980b9);
  color: white;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(52, 152, 219, 0.4);
  /* 마우스 호버 시의 전환 효과를 제거했습니다. */
  transition: background-color 0.2s; /* 클릭 효과를 위한 부드러운 전환은 유지 */
}

/* 마우스 호버 시의 스타일 변경을 제거했습니다. */

.save-btn:active {
  transform: translateY(1px); /* 클릭 시 살짝 눌리는 효과 */
  box-shadow: 0 2px 10px rgba(52, 152, 219, 0.3);
}
</style>