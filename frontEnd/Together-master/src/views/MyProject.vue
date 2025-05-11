<template>
  <div class="project-container">
    <Sidebar />

    <main class="main-content">
      <!-- 프로젝트 상단 정보 -->
      <section class="project-header">
        <img src="@/assets/togetherlogo.png" alt="프로젝트 로고" class="project-logo" />
        <div class="project-info">
          <input
            v-model="projectName"
            class="project-name"
            placeholder="프로젝트 이름"
          />
          <textarea
            v-model="projectDescription"
            class="project-description"
            placeholder="프로젝트에 대한 설명을 적어주세요"
          ></textarea>
          <button class="save-btn" @click="saveProjectInfo">저장</button>
        </div>
        <div class="vertical-line"></div>
        <div class="progress-container">
          <div class="progress-number">{{ progress }}%</div>
          <div class="progress-row">
            <span class="progress-label">작업 진행도</span>
            <div class="progress-bar">
              <div class="progress" :style="{ width: progress + '%' }"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- 단계별 요약 정보 -->
      <section class="project-steps">
        <div
          class="step"
          v-for="(step, idx) in steps"
          :key="idx"
          @click="selectStep(step.name)"
          :class="{ active: selectedStep === step.name }"
        >
          <h4>{{ step.current }}/{{ step.total }}</h4>
          <p>{{ step.name }}</p>
        </div>
      </section>

      <!-- 상세 입력 컴포넌트 -->
      <component
        :is="currentDetailComponent"
        v-if="selectedStep"
        @updateStepProgress="updatePlanningProgress"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import Sidebar from '@/components/Sidebar.vue'
import PlanningDetails from '@/components/PlanningDetails.vue'
import DesignDetails from '@/components/DesignDetails.vue'
import DevelopmentDetails from '@/components/DevelopmentDetails.vue'
import TestingDetails from '@/components/TestingDetails.vue'

const projectId          = ref(null)
const projectName        = ref('로딩 중...')
const projectDescription = ref('')

// 기존 단계 정보
const steps = ref([
  { name: '기획', current: 0, total: 5 },
  { name: '설계', current: 1, total: 8 },
  { name: '개발', current: 1, total: 4 },
  { name: '테스트', current: 1, total: 3 },
])
const selectedStep = ref('기획')

// **할 일 목록**을 불러와서 progress 계산
// status 필드가 'PENDING' 또는 'COMPLETED' 라고 가정
const tasks = ref([])

const progress = computed(() => {
  const total       = tasks.value.length
  const completed   = tasks.value.filter(t => t.status === 'COMPLETED').length
  return total ? Math.round((completed / total) * 100) : 0
})

function selectStep(stepName) {
  selectedStep.value = stepName
}

const currentDetailComponent = computed(() => {
  switch (selectedStep.value) {
    case '기획':   return PlanningDetails
    case '설계':   return DesignDetails
    case '개발':   return DevelopmentDetails
    case '테스트': return TestingDetails
    default:       return null
  }
})

// 자식 컴포넌트에서 완료 개수 업데이트
function updatePlanningProgress(count) {
  const step = steps.value.find(s => s.name === selectedStep.value)
  if (step) step.current = count
}

// 프로젝트 제목/설명 저장
const saveProjectInfo = async () => {
  if (!projectId.value) {
    alert('프로젝트 ID를 불러오지 못했습니다.')
    return
  }
  try {
    await axios.put(
      `/projects/${projectId.value}/update-title`,
      { newTitle: projectName.value },
      { headers: { Authorization: localStorage.getItem('authHeader') }, withCredentials: true }
    )
    const formData = new FormData()
    formData.append('type', 'description')
    formData.append('text', projectDescription.value)
    await axios.post(
      '/planning/upload',
      formData,
      { headers: { Authorization: localStorage.getItem('authHeader') }, withCredentials: true }
    )
    alert('프로젝트 제목과 설명이 저장되었습니다.')
  } catch (err) {
    console.error('❌ 저장 오류:', err)
    alert('저장 중 오류가 발생했습니다.')
  }
}

// 초기 로드: 프로젝트 + tasks
onMounted(async () => {
  try {
    // ✅ 1단계: 내 프로젝트 정보 가져오기
    const projectRes = await axios.get('/projects/my', {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
      },
      withCredentials: true,
    })

    // 프로젝트 ID와 이름 설정
    projectId.value = projectRes.data.projectId
    projectName.value = projectRes.data.title

    // ✅ 2단계: 프로젝트 설명 가져오기
    const detailRes = await axios.get('/planning/all', {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
      },
      withCredentials: true,
    })
    projectDescription.value = detailRes.data.description?.text || ''

    // ✅ 3단계: 프로젝트 ID가 설정된 다음에 tasks 불러오기!
    if (projectId.value) {
const taskRes = await axios.get('/work-tasks/project', {
  headers: {
    Authorization: localStorage.getItem('authHeader'),
  },
  withCredentials: true
})


      tasks.value = taskRes.data // 여기서 [{ id, title, status: 'PENDING'|'COMPLETED' }, ...]
    } else {
      console.warn('⚠️ 프로젝트 ID가 없습니다. tasks를 불러올 수 없습니다.')
    }
  } catch (err) {
    console.error('❌ 데이터 로딩 실패:', err)
    if (err.response?.status === 403) {
      alert('작업 목록을 불러올 수 없습니다. 접근 권한이 없거나 로그인 상태가 만료되었을 수 있습니다.')
    }
  }
})

</script>



<style scoped>
html, body {
  margin: 0;
  padding: 0;
  overflow: hidden;  /* ✅ 페이지 전체 스크롤 제거 */
  height: 100%;
}
.project-container {
  display: flex;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background-color: #fafafa;
}

.main-content {
  margin-left: 220px;
  margin-right: 18px;
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.project-header {
  display: flex;
  align-items: center;
  background: white;
  border-radius: 10px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.project-logo {
  width: 60px;
  height: 60px;
  margin-right: 15px;
}
.project-info {
  display: flex;
  flex-direction: column;
  flex: 1;
  gap: 10px;
}
.project-name {
  font-size: 1.5rem;
  font-weight: bold;
  border: none;
  outline: none;
  background: none;
}
.project-description {
  height: 50px;
  border: none;
  outline: none;
  background: none;
  resize: none;
}
.save-btn {
  align-self: flex-start;
  background-color: #3f8efc;
  color: white;
  border: none;
  padding: 6px 12px;
  margin-top: 8px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.9rem;
}
.save-btn:hover {
  background-color: #2e6fd8;
}
.vertical-line {
  width: 1px;
  height: 80px;
  background-color: #ddd;
  margin: 0 20px;
}

.progress-container {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}
.progress-number {
  font-size: 2rem;
  color: #3f8efc;
  font-weight: bold;
  margin: 0;
}
.progress-row {
  display: flex;
  align-items: center;
  gap: 10px;
}
.progress-label {
  font-size: 1rem;
  color: #666;
  white-space: nowrap;
}
.progress-bar {
  width: 400px;
  height: 10px;
  background: #ddd;
  border-radius: 5px;
  overflow: hidden;
}
.progress {
  height: 100%;
  background: #3f8efc;
}

.project-steps {
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: white;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.step {
  cursor: pointer;
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 0 10px;
  transition: background-color 0.3s;
}
.step h4 {
  color: #3f8efc;
  font-size: 1.2rem;
  margin: 0;
  margin-bottom: 5px;
}
.step p {
  font-size: 0.9rem;
  margin: 0;
}
.step:not(:last-child)::after {
  content: "";
  position: absolute;
  top: 15%;
  bottom: 15%;
  right: 0;
  width: 1px;
  background-color: #ddd;
}
.step.active {
  background-color: #e6f0ff;
  border-radius: 8px;
}
.step.active h4,
.step.active p {
  font-weight: bold;
  color: #3f8efc;
}
.timeline {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}
.timeline-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.circle {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #ddd;
}
.circle.active {
  background: #3f8efc;
}
.timeline-text {
  font-size: 0.8rem;
  margin-top: 5px;
}
</style>
