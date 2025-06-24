<template>
  <div class="project-container">
    <aside class="sidebar">
      <section class="card project-info-card">
        <input
          type="file"
          ref="fileInput"
          accept="image/*"
          style="display: none"
          @change="handleImageChange"
        />
        <div class="logo-wrapper" @click="!isReadOnly && triggerImageUpload()">
           <!-- 프로젝트 이미지가 있을 때만 프로젝트 이미지를 렌더링, 없으면 기본 로고 -->
  <img
    v-if="projectImageUrl"
    :src="projectImageUrl"
    :key="projectImageUrl"  
    alt="프로젝트 로고"
    class="project-logo"
    referrerpolicy="no-referrer"
  />
  <img
    v-else
    :src="defaultLogo"
    alt="기본 로고"
    class="project-logo"
    referrerpolicy="no-referrer"
  />
        </div>

        <!-- 이름 입력 박스: 가로 중앙 고정, 세로 중앙 정렬 -->
        <div class="name-container">
          <input
            v-model="projectName"
            ref="nameRef"
            class="project-name"
            placeholder="프로젝트 이름"
            :readonly="isReadOnly"
            @input="autoSizeName"
          />
        </div>

        <textarea
          v-model="projectDescription"
          ref="descRef"
          class="project-description"
          placeholder="프로젝트 설명을 입력하세요"
          :readonly="isReadOnly"
          @input="autoResizeDescription"
        ></textarea>

        <div class="team-list">
          <span class="member" v-for="member in teamMembers" :key="member.id">
            {{ member.name }}
          </span>
        </div>

        <div class="progress-container">
          <div class="progress-label">작업 진행도 <strong>{{ progress }}%</strong></div>
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
        </div>
      </section>

      <section class="card steps-card">
        <div
          class="step-btn"
          v-for="(step, idx) in steps"
          :key="idx"
          :class="{ active: selectedStep === step.name }"
          @click="selectStep(step.name)"
        >
          <div class="step-count">{{ step.current }}/{{ step.total }}</div>
          <div class="step-name">{{ step.name }}</div>
        </div>
      </section>
      <!-- steps-card 아래에 추가 -->
<section class="card export-card">
  <button class="export-btn" @click="downloadPdf" :disabled="!projectId">
    📄 PDF 문서 추출
  </button>
        <button
          v-if="!isReadOnly"
          class="leave-btn"
          @click="leaveProject"
      >
        프로젝트 탈퇴
      </button>
</section>

    </aside>

    <main class="detail-panel">
      <component
        :is="currentDetailComponent"
        v-if="selectedStep && projectId"
        :project-id="projectId"
        :readonly="isReadOnly"
        @updateStepProgress="updatePlanningProgress"
      />
      <FloatingHelpWidget @open-help="showHelp = true" />
      <HelpModal v-if="showHelp" @close="showHelp = false" />
    </main>
  </div>
</template>
<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { debounce } from 'lodash'

import PlanningDetails from '@/components/PlanningDetails.vue'
import DesignDetails from '@/components/DesignDetails.vue'
import DevelopmentDetails from '@/components/DevelopmentDetails.vue'
import TestingDetails from '@/components/TestingDetails.vue'
import defaultLogo from '@/assets/togetherlogo.png'
import FloatingHelpWidget from '@/components/FloatingHelpWidget.vue'
import HelpModal from '@/components/HelpModal.vue'

const route = useRoute()
const router = useRouter()
const isReadOnly = computed(() => route.query.readonly === 'true')
const routeProjectId = computed(() => Number(route.params.projectId))

const projectId = ref(null)
const projectName = ref('로딩 중...')
const projectDescription = ref('')
const projectImageUrl = ref('')
const teamMembers = ref([])
const fileInput = ref(null)
const showHelp = ref(false)
function triggerImageUpload() {
  fileInput.value.click()
}

// 👇 여기서부터 추가!
async function downloadPdf() {
  if (!projectId.value) return;
  try {
    const response = await axios.get(
      `/export/pdf`,
      {
        params: { projectId: projectId.value },
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true,
        responseType: 'blob',
      }
    );
    const url = window.URL.createObjectURL(new Blob([response.data]));
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `project_${projectId.value}.pdf`);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    alert('PDF 추출에 실패했습니다.');
    console.error(err);
  }
}

async function leaveProject() {
  if (!projectId.value) return;
  if (!confirm('정말로 프로젝트를 탈퇴하시겠습니까?')) return;
  try {
    await axios.delete('/projects/leave', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true,
    });
    alert('프로젝트에서 성공적으로 나갔습니다.');
    router.push('/MainPage2');
  } catch (err) {
    alert('프로젝트 탈퇴에 실패했습니다.');
    console.error(err);
  }
}
// 👆 여기까지 추가!

async function handleImageChange(event) {
  const file = event.target.files[0]
  if (!file || !projectId.value || isReadOnly.value) return

  try {
    const formData = new FormData()
    formData.append('image', file)

    const { data } = await axios.put('/projects/image', formData, {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
        'Content-Type': 'multipart/form-data',
      },
      withCredentials: true,
    })

    projectImageUrl.value = data
  } catch (err) {
    console.error('❌ 이미지 업로드 실패:', err)
    alert('이미지 업로드 중 오류가 발생했습니다.')
  }
}

const steps = ref([
  { name: '기획', current: 0, total: 5 },
  { name: '설계', current: 0, total: 8 },
  { name: '개발', current: 0, total: 4 },
  { name: '테스트', current: 0, total: 3 },
])
const selectedStep = ref('기획')
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

const autoSaveProjectInfo = debounce(async () => {
  if (!projectId.value || isReadOnly.value) return

  try {
    await axios.put(
      `/projects/${projectId.value}/update-title`,
      { newTitle: projectName.value },
      { headers: { Authorization: localStorage.getItem('authHeader') }, withCredentials: true }
    )

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

if (!isReadOnly.value) {
  watch([projectName, projectDescription], autoSaveProjectInfo, { flush: 'post' })
}

onMounted(async () => {
  try {
    // 프로젝트 정보 불러오기
    let projectRes
    if (isReadOnly.value) {
      projectRes = await axios.get(`/projects/${routeProjectId.value}`, {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
    } else {
      projectRes = await axios.get('/projects/my', {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
    }

    // 📌 콘솔로 응답 객체 전체 확인
    console.log('프로젝트 응답:', projectRes.data);
    // 콘솔로 imageUrl 값만 확인
    console.log('imageUrl:', projectRes.data.imageUrl);

    projectId.value = projectRes.data.projectId
    projectName.value = projectRes.data.title
    projectImageUrl.value = projectRes.data.imageUrl || ''

    // 기획 데이터
    const planningRes = await axios.get('/planning/all', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    projectDescription.value = planningRes.data.description?.text || ''

    const planningTypes = ['motivation', 'goal', 'requirement', 'infostructure', 'storyboard']
    const planningCount = planningTypes.filter(type => {
      const entry = planningRes.data[type]
      return entry?.text?.trim() !== '' || (entry?.files?.length > 0)
    }).length
    steps.value.find(s => s.name === '기획').current = planningCount

    // 설계 데이터
    const designRes = await axios.get('/design/all', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    const designTypes = ['usecase', 'classDiagram', 'sequence', 'ui', 'erd', 'table', 'architecture', 'schedule']
    const designCount = designTypes.filter(type => {
      const entry = designRes.data[type]
      if (!entry) return false
      const hasText = entry.text && entry.text.trim() !== ''
      const hasJson = entry.json && entry.json.trim() !== ''
      const hasFiles = Array.isArray(entry.files) && entry.files.length > 0
      return hasText || hasJson || hasFiles
    }).length
    steps.value.find(s => s.name === '설계').current = designCount
// 개발 데이터
const developRes = await axios.get('/develop/all', {
  params: { projectId: projectId.value },
  headers: { Authorization: localStorage.getItem('authHeader') },
  withCredentials: true
})

// 백엔드 DevelopAllResponseDto의 키와 동일하게 맞춤
const developTypes = ['environment', 'versioning', 'commitRule', 'folder']

const developCount = developTypes.filter(type => {
  const entry = developRes.data[type]
  if (!entry) return false

  // 텍스트가 있을 때
  const hasText = entry.text && entry.text.trim() !== ''
  // 파일이 있을 때
  const hasFiles = Array.isArray(entry.files) && entry.files.length > 0

  return hasText || hasFiles
}).length

// steps 배열에서 '개발' 스텝의 current 값 업데이트
steps.value.find(s => s.name === '개발').current = developCount

    // 작업 항목
    const taskRes = await axios.get('/work-tasks/project', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    tasks.value = taskRes.data

    // 팀원
    const memberRes = await axios.get('/projects/members/students', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    teamMembers.value = memberRes.data
        .filter(member => member.role === 'STUDENT')
        .map(member => ({ name: member.userName, id: member.userId }))
  } catch (err) {
    console.error('❌ 데이터 로딩 실패:', err)
  }
})
</script>

<style scoped>
.project-container {
  display: flex;
  gap: 10px;
  padding: 24px;
  background-color: #fafcff;
}

.sidebar {
  width: 320px;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.card {
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.project-info-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.logo-wrapper {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: #f0f0f0;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  cursor: pointer;
}

.project-logo {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
}

.name-container {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 22px;           /* 고정 높이 */
  margin-bottom: 16px;
}

.project-name {
  font-size: 18px;
  font-weight: 600;
  border: none;
  outline: none;
  background: transparent;
  width: auto;
  min-width: 120px;
  max-width: 100%;
  white-space: pre;
  text-align: center;
}

.project-description {
  width: 100%;
  min-height: 70px;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  padding: 12px;
  resize: none;
  margin-bottom: 16px;
}

.team-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.member {
  background: #eef6ff;
  color: #0066cc;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.progress-container {
  width: 100%;
}

.progress-label {
  font-size: 13px;
  color: #333;
  margin-bottom: 8px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #f2f2f2;
  border-radius: 4px;
  overflow: hidden;
  margin-bottom: 0;
}

.progress-fill {
  height: 100%;
  background: #4a90e2;
  transition: width 0.3s ease;
}

.steps-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.step-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 6px 0;
  background: #f9f9f9;
  border-radius: 12px;
  cursor: pointer;
  transition: background 0.2s;
  height: 55px;
}

.step-btn:hover {
  background: #eef2f8;
}

.step-btn.active {
  background: #e6f0ff;
}

.step-count {
  font-size: 12px;
  color: #555;
  margin-bottom: 4px;
}

.step-name {
  font-size: 14px;
  font-weight: 600;
  color: #222;
}

.detail-panel {
  flex: 1;
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  min-height: 600px;
}
.export-card {
  padding: 1rem;
  text-align: center;
}

.export-btn {
  width: 100%;
  padding: 0.6rem;
  background-color: #72b9ff;
  color: white;
  font-weight: bold;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.export-btn:hover {
  background-color: #148aff;
}

.leave-btn {
  width: 100%;
  margin-top: 10px;
  padding: 8px 0;
  background: #f44336;
  color: #fff;
  border: none;
  border-radius: 8px;
  cursor: pointer;
}
.leave-btn:hover {
  background: #d32f2f;
}
</style>
