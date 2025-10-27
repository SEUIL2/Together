<template>
  <div class="dashboard-container">
    <div class="card environment-setup-card">
      <div class="card-header">
        <div class="card-title-wrapper">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M12 11v8M12 3v4M4.5 8.5l3-3M16.5 18.5l3-3M21.5 15.5l-3 3M4.5 15.5l3 3M8.5 4.5l-3 3M18.5 4.5l-3 3"/>
            <circle cx="12" cy="12" r="2"/>
          </svg>
          <h3 class="board-title">프로젝트 개발 환경</h3>
        </div>
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
            <button type="submit" class="save-btn">저장</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/api';
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
  if (!projectId.value) {
    console.log('⚠️ projectId가 없어서 개발 환경을 불러올 수 없습니다.');
    return;
  }
  console.log('📥 개발 환경 조회 - projectId:', projectId.value);
  try {
    const response = await api.get('/api/dev-env', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    console.log('✅ 개발 환경 데이터:', response.data);
    // 프로젝트에 설정된 첫 번째 환경 정보를 불러옴
    if (response.data && response.data.length > 0) {
      environment.value = response.data[0];
    }
  } catch (error) {
    console.error('❌ 개발 환경 정보를 불러오는 데 실패했습니다.', error);
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
      response = await api.put(`/api/dev-env/${environment.value.id}`, requestDto, {
        headers: { Authorization: localStorage.getItem('authHeader') }
      });
    } else {
      response = await api.post('/api/dev-env', requestDto, {
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
  // URL에서 프로젝트 ID 가져오기 (route.params, route.query, 사용자 정보 순으로 확인)
  console.log('🔍 route.params:', route.params);
  console.log('🔍 route.query:', route.query);
  
  try {
    const { data: me } = await api.get('/auth/me', { withCredentials: true });
    console.log('👤 사용자 정보:', me);
    
    // 우선순위: route.params > route.query > me.projectId
    projectId.value = route.params.projectId || route.query.projectId || me.projectId;
    
    console.log('✅ 최종 projectId:', projectId.value);
    
    if (projectId.value) {
      await fetchEnvironment();
    } else {
      console.warn('⚠️ projectId를 찾을 수 없습니다.');
    }
  } catch (error) {
    console.error('❌ 사용자 정보 조회 실패:', error);
  }
});
</script>

<style scoped>
.dashboard-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: #f7f8fc;
}
.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  margin-top: -10px;
}
.card-title-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #333;
}
.board-title {
  font-size: 20px;
  cursor: default;
}
.card-body {
  flex-grow: 1;
  overflow-y: auto;
}
.environment-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}
.form-group {
  display: flex;
  flex-direction: column;
}
.form-group.full-width {
  grid-column: 1 / -1;
}
.form-group label {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #343a40;
}
.form-group input,
.form-group textarea {
  padding: 12px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 15px;
  background-color: #f8f9fa;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3f8efc;
  box-shadow: 0 0 0 2px rgba(63, 142, 252, 0.2);
}
.form-group textarea {
  min-height: 100px;
  resize: vertical;
}
.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
.save-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  background-color: #3f8efc;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}
.save-btn:hover {
  background-color: #2a7de9;
}
</style>