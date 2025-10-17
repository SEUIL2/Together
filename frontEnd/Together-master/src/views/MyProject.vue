<template>
  <div class="project-container">
    <!-- <div v-if="saveStatus === 'saving'" class="save-toast saving">저장 중...</div>
    <div v-else-if="saveStatus === 'saved'" class="save-toast saved">💾 저장 완료</div>
    <div v-else-if="saveStatus === 'error'" class="save-toast error">저장 실패!</div> -->

    <main class="detail-panel">
      <component
        :is="currentDetailComponent"
        v-if="selectedStep && projectId"
        :project-id="projectId"
        :readonly="isReadOnly"
        :substep="route.query.substep"
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
import api from '@/api' // ✅ axios 대신 api 인스턴스 사용
import { debounce } from 'lodash'

import PlanningDetails from '@/components/myproject/PlanningDetails.vue'
import DesignDetails from '@/components/myproject/DesignDetails.vue'
import DevelopmentDetails from '@/components/myproject/DevelopmentDetails.vue'
import TestingDetails from '@/components/TestingDetails.vue'
import defaultLogo from '@/assets/togetherlogo.png'
import FloatingHelpWidget from '@/components/myproject/FloatingHelpWidget.vue'
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
const saveStatus = ref('idle'); // 'idle', 'saving', 'saved', 'error'

async function handleImageChange(event) {
  const file = event.target.files[0]
  if (!file || !projectId.value || isReadOnly.value) return

  try {
    const formData = new FormData()
    formData.append('image', file)

    const { data } = await api.put('/projects/image', formData)

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
  { name: '테스트', current: 0, total: 2 },
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
  saveStatus.value = 'saving';

  try {
    await api.put(
      `/projects/${projectId.value}/update-title`,
      { newTitle: projectName.value }
    )

    await api.put(
      '/planning/update', 
      { type: 'description', text: projectDescription.value, projectId: projectId.value }
    )
    saveStatus.value = 'saved';
    setTimeout(() => saveStatus.value = 'idle', 2000);
  } catch (err) {
    console.error('❌ 자동 저장 실패:', err)
    saveStatus.value = 'error';
    setTimeout(() => saveStatus.value = 'idle', 3000);
  }
}, 800)

if (!isReadOnly.value) {
  watch([projectName, projectDescription], autoSaveProjectInfo, { flush: 'post' })
}

watch(() => route.query.step, (newStep) => {
  if (newStep && steps.value.some(s => s.name === newStep)) {
    selectedStep.value = newStep;
  }
}, { immediate: true });


onMounted(async () => {
  try {
    // 프로젝트 정보 불러오기
    let projectRes
    if (isReadOnly.value) {
      projectRes = await api.get(`/projects/${routeProjectId.value}`)
    } else {
      projectRes = await api.get('/projects/my')
    }

    // 📌 콘솔로 응답 객체 전체 확인
    console.log('프로젝트 응답:', projectRes.data);
    // 콘솔로 imageUrl 값만 확인
    console.log('imageUrl:', projectRes.data.imageUrl);

    projectId.value = projectRes.data.projectId
    projectName.value = projectRes.data.title
    projectImageUrl.value = projectRes.data.imageUrl || ''

    // 기획 데이터
    const planningRes = await api.get('/planning/all', {
      params: { projectId: projectId.value }
    })
    projectDescription.value = planningRes.data.description?.text || ''

    const planningTypes = ['motivation', 'goal', 'requirement', 'infostructure', 'storyboard']
    const planningCount = planningTypes.filter(type => {
      const entry = planningRes.data[type]
      return entry?.text?.trim() !== '' || (entry?.files?.length > 0)
    }).length
    steps.value.find(s => s.name === '기획').current = planningCount

    // 설계 데이터
    const designRes = await api.get('/design/all', {
      params: { projectId: projectId.value }
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
const developRes = await api.get('/develop/all', {
  params: { projectId: projectId.value }
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
    const taskRes = await api.get('/work-tasks/project', {
      params: { projectId: projectId.value }
    })
    tasks.value = taskRes.data

    // 팀원
    const memberRes = await api.get('/projects/members/students', {
      params: { projectId: projectId.value }
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
  padding: 24px;
  background-color: #fafcff;
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
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  min-height: 600px;
}

.save-toast {
  position: fixed;
  top: 80px; /* 헤더 아래 */
  right: 24px;
  z-index: 2000;
  background: #333;
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
  padding: 10px 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transition: opacity .3s, transform .3s;
  pointer-events: none;
}
.save-toast.saved { background: #28a745; }
.save-toast.error { background: #dc3545; }
</style>
