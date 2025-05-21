<template>
  <div class="project-container">
    <main class="main-content">
      <section class="project-header">
        <input
          type="file"
          ref="fileInput"
          accept="image/*"
          style="display: none"
          @change="handleImageChange"
        />
        <img
          :src="projectImageUrl || defaultLogo"
          :key="projectImageUrl"
          alt="프로젝트 로고"
          class="project-logo"
          @click="!isReadOnly && triggerImageUpload()"
          referrerpolicy="no-referrer"
        />

        <div class="project-info">
          <input
            v-model="projectName"
            class="project-name"
            placeholder="프로젝트 이름"
            :readonly="isReadOnly"
          />
          <textarea
            v-model="projectDescription"
            class="project-description"
            placeholder="프로젝트에 대한 설명을 적어주세요"
            :readonly="isReadOnly"
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
<component
  :is="currentDetailComponent"
  v-if="selectedStep && projectId"
  :project-id="projectId"
  :readonly="isReadOnly"
  @updateStepProgress="updatePlanningProgress"
/>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import { debounce } from 'lodash'

import PlanningDetails from '@/components/PlanningDetails.vue'
import DesignDetails from '@/components/DesignDetails.vue'
import DevelopmentDetails from '@/components/DevelopmentDetails.vue'
import TestingDetails from '@/components/TestingDetails.vue'
import defaultLogo from '@/assets/togetherlogo.png'

const route = useRoute()
const isReadOnly = computed(() => route.query.readonly === 'true')
const routeProjectId = computed(() => Number(route.params.projectId))

const projectId = ref(null)
const projectName = ref('로딩 중...')
const projectDescription = ref('')
const projectImageUrl = ref('')
const teamMembers = ref([])
const fileInput = ref(null)

function triggerImageUpload() {
  fileInput.value.click()
}

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
    // 1. 🔍 프로젝트 정보
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

    projectId.value = projectRes.data.projectId
    projectName.value = projectRes.data.title
    projectImageUrl.value = projectRes.data.imageUrl || ''

    // 2. 🔍 기획 항목
    const planningRes = await axios.get('/planning/all', {
      params: { projectId: projectId.value }, // ✅ 항상 명확히 전달
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

    // 3. 🔍 설계 항목
    const designRes = await axios.get('/design/all', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

const designTypes = [
  'usecase', 'classDiagram', 'sequence', 'ui',
  'erd', 'table', 'architecture', 'schedule'
]
const designCount = designTypes.filter(type => {
  const entry = designRes.data[type]
  if (!entry) return false
  const hasText = entry.text && entry.text.trim() !== ''
  const hasJson = entry.json && entry.json.trim() !== ''
  const hasFiles = Array.isArray(entry.files) && entry.files.length > 0
  return hasText || hasJson || hasFiles
}).length
steps.value.find(s => s.name === '설계').current = designCount


    // 4. 🔍 작업 항목
    const taskRes = await axios.get('/work-tasks/project', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    tasks.value = taskRes.data

    // 5. 🔍 팀원
    const memberRes = await axios.get('/projects/members', {
      params: { projectId: projectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    teamMembers.value = memberRes.data.map(member => ({
      name: member.userName,
      id: member.userId
    }))

  } catch (err) {
    console.error('❌ 데이터 로딩 실패:', err)
  }
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
.project-logo {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  object-fit: cover;
  cursor: pointer;
  transition: 0.2s;
  padding: 5px;
}

.project-logo:hover {
  opacity: 0.8;
}

</style>
