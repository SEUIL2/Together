<template>
  <div class="project-container">
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
          <div class="team-list">
  <span class="member" v-for="member in teamMembers" :key="member.id">
    {{ member.name }}
  </span>
</div>
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
import { ref, computed, onMounted, watch } from 'vue'
import axios from 'axios'
import { debounce } from 'lodash'

import PlanningDetails from '@/components/PlanningDetails.vue'
import DesignDetails from '@/components/DesignDetails.vue'
import DevelopmentDetails from '@/components/DevelopmentDetails.vue'
import TestingDetails from '@/components/TestingDetails.vue'

const projectId = ref(null)
const projectName = ref('로딩 중...')
const projectDescription = ref('')
const teamMembers = ref([])

// 단계 목록
const steps = ref([
  { name: '기획', current: 0, total: 5 },
  { name: '설계', current: 1, total: 8 },
  { name: '개발', current: 1, total: 4 },
  { name: '테스트', current: 1, total: 3 },
])
const selectedStep = ref('기획')

// 작업 목록
const tasks = ref([])

const progress = computed(() => {
  const total = tasks.value.length
  const completed = tasks.value.filter(t => t.status === 'COMPLETED').length
  return total ? Math.round((completed / total) * 100) : 0
})

function selectStep(stepName) {
  selectedStep.value = stepName
}

const currentDetailComponent = computed(() => {
  switch (selectedStep.value) {
    case '기획': return PlanningDetails
    case '설계': return DesignDetails
    case '개발': return DevelopmentDetails
    case '테스트': return TestingDetails
    default: return null
  }
})

function updatePlanningProgress(count) {
  const step = steps.value.find(s => s.name === selectedStep.value)
  if (step) step.current = count
}

// ✅ 자동 저장 함수 (debounce 적용)
const autoSaveProjectInfo = debounce(async () => {
  if (!projectId.value) return

  try {
    // 제목 저장
    await axios.put(
      `/projects/${projectId.value}/update-title`,
      { newTitle: projectName.value },
      { headers: { Authorization: localStorage.getItem('authHeader') }, withCredentials: true }
    )

    // 설명 저장
    const formData = new FormData()
    formData.append('type', 'description')
    formData.append('text', projectDescription.value)

    await axios.put(
      '/planning/update',
      formData,
      { headers: { Authorization: localStorage.getItem('authHeader') }, withCredentials: true }
    )
  } catch (err) {
    console.error('❌ 자동 저장 실패:', err)
  }
}, 800)

// ✅ 입력 변경 감지 시 자동 저장
watch([projectName, projectDescription], autoSaveProjectInfo, { flush: 'post' })

async function fetchTeamMembers() {
  try {
    const { data } = await axios.get('/projects/members', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    teamMembers.value = data.map(member => ({
      name: member.userName, // 이걸 기준으로 출력 가능하게 만듦
      id: member.userId
    }))
  } catch (err) {
    console.error('❌ 팀원 정보 불러오기 실패:', err)
  }
}


onMounted(async () => {
  try {
    const projectRes = await axios.get('/projects/my', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    })

    projectId.value = projectRes.data.projectId
    projectName.value = projectRes.data.title

    const detailRes = await axios.get('/planning/all', {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    })
    projectDescription.value = detailRes.data.description?.text || ''

    if (projectId.value) {
      const taskRes = await axios.get('/work-tasks/project', {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
      tasks.value = taskRes.data
    }
  } catch (err) {
    console.error('❌ 데이터 로딩 실패:', err)
    if (err.response?.status === 403) {
      alert('작업 목록을 불러올 수 없습니다. 접근 권한이 없거나 로그인 상태가 만료되었을 수 있습니다.')
    }
  }
  await fetchTeamMembers()
})
</script>


<style scoped>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  overflow: auto;  /* ← ✅ 여기에만 스크롤 허용 */
}

.project-container {
  display: block;
  width: 100%;
  background-color: #fafafa;
}


.main-content {
  margin: 0 auto;
  padding: 20px;
  max-width: 1100px;
  width: 100%;
  /* overflow-y: auto; ← ❌ 이거 지워 */
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
  height: 20px;
  border: none;
  outline: none;
  background: none;
  resize: none;
}

.team-list {
  margin-top: 10px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.member {
  background-color: #eef4ff;
  color: #3f8efc;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 0.7rem;
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
