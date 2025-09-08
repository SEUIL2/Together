<template>
  <div class="pi-card">
    <!-- 숨김 파일 입력 -->
    <input
      type="file"
      ref="fileInput"
      accept="image/*"
      style="display:none"
      @change="handleImageChange"
    />

    <!-- 헤더: 로고 + 이름 -->
    <div class="pi-header">
      <button class="logo-wrapper" :disabled="isReadOnly" @click="triggerImageUpload">
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
      </button>

      <div class="name-container">
        <input
          v-model="projectName"
          ref="nameRef"
          class="project-name"
          placeholder="프로젝트 이름"
          :readonly="isReadOnly"
          @input="autoSizeName"
        />
        <span class="pi-badge">진행 {{ progress }}%</span>
      </div>
    </div>

    <!-- 설명 -->
    <textarea
      v-model="projectDescription"
      ref="descRef"
      class="project-description"
      placeholder="프로젝트 설명을 입력하세요"
      :readonly="isReadOnly"
      @input="autoResizeDescription"
    ></textarea>

    <!-- 팀원 -->
<!-- 팀원 -->
<div class="team-list">
  <span class="member" v-for="member in teamMembers" :key="member.id || member.name">
    {{ member.name }}
  </span>
</div>

    <!-- 진행도 -->
    <div class="progress-container">
      <div class="progress-top">
        <div class="progress-label">작업 진행도</div>
        <div class="progress-state" :data-state="healthState">{{ healthText }}</div>
      </div>
      <div class="progress-bar">
        <div class="progress-fill" :style="{ width: progress + '%' }"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import axios from 'axios'
import { debounce } from 'lodash'
import defaultLogo from '@/assets/togetherlogo.png'

const props = defineProps({
  projectId: { type: [String, Number], default: null },
  projectName: { type: String, default: '' },
  projectDescription: { type: String, default: '' },
  teamMembers: { type: Array, default: () => [] }, // [{id, name}]
  projectImageUrl: { type: String, default: '' },
  progress: { type: Number, default: 0 },
  isReadOnly: { type: Boolean, default: false }
})

const projectName = ref(props.projectName)
const projectDescription = ref(props.projectDescription)
const projectImageUrl = ref(props.projectImageUrl)

watch(() => props.projectName, v => (projectName.value = v))
watch(() => props.projectDescription, v => (projectDescription.value = v))
watch(() => props.projectImageUrl, v => (projectImageUrl.value = v))

const fileInput = ref(null)
const nameRef = ref(null)
const descRef = ref(null)

function triggerImageUpload() {
  if (props.isReadOnly) return
  fileInput.value?.click()
}

async function handleImageChange(event) {
  const file = event.target.files[0]
  if (!file || !props.projectId || props.isReadOnly) return
  try {
    const formData = new FormData()
    formData.append('image', file)
    const { data } = await axios.put('/projects/image', formData, {
      headers: {
        Authorization: localStorage.getItem('authHeader'),
        'Content-Type': 'multipart/form-data'
      },
      withCredentials: true
    })
    projectImageUrl.value = data
  } catch (err) {
    console.error('❌ 이미지 업로드 실패:', err)
    alert('이미지 업로드 중 오류가 발생했습니다.')
  }
}

function autoSizeName() {
  const el = nameRef.value
  if (!el) return
  el.style.width = 'auto'
  el.style.width = `${el.scrollWidth}px`
}

function autoResizeDescription() {
  const el = descRef.value
  if (!el) return
  el.style.height = 'auto'
  el.style.height = `${el.scrollHeight}px`
}

const autoSaveProjectInfo = debounce(async () => {
  if (!props.projectId || props.isReadOnly) return
  try {
    await axios.put(
      `/projects/${props.projectId}/update-title`,
      { newTitle: projectName.value },
      {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      }
    )
    const formData = new FormData()
    formData.append('type', 'description')
    formData.append('text', projectDescription.value)
    await axios.put('/planning/update', formData, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
  } catch (err) {
    console.error('❌ 자동 저장 실패:', err)
  }
}, 800)

if (!props.isReadOnly) {
  watch([projectName, projectDescription], autoSaveProjectInfo, { flush: 'post' })
}

/* === UI 보조 로직 === */
const initials = (name = '') =>
  name
    .split(' ')
    .map(s => s[0])
    .join('')
    .slice(0, 2)
    .toUpperCase()

const healthState = computed(() => {
  if (props.progress >= 80) return 'good'
  if (props.progress >= 50) return 'warn'
  return 'risk'
})
const healthText = computed(() => {
  const s = healthState.value
  return s === 'good' ? '양호' : s === 'warn' ? '보통' : '위험'
})
</script>
<style scoped>
:root{
  --brand:#2563eb;     /* 포인트 블루 */
  --brand2:#60a5fa;    /* 보조 블루 */
  --text:#0f172a;
  --muted:#64748b;
  --bg:#ffffff;
  --line:rgba(15,23,42,.08);
  --shadow:0 6px 18px rgba(2,6,23,.06);
}

/* 카드 */
.pi-card{
  display:flex; flex-direction:column; gap:14px;
  padding:16px 16px 18px;
  background: var(--bg);
  border:1px solid var(--line);
  border-radius:14px;
  box-shadow: var(--shadow);
  transform: translateY(-4px);
}

/* 헤더: 로고 + 이름 */
.pi-header{ display:flex; align-items:center; gap:14px; }
.logo-wrapper{
  width:84px; height:84px; border-radius:16px;
  display:flex; align-items:center; justify-content:center;
  background:#f8fafc;
  border:1px solid var(--line);
  cursor:pointer;
  transition: transform .15s ease, box-shadow .15s ease;
}
.logo-wrapper:hover:not(:disabled){
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(2,6,23,.08);
}
.project-logo{
  width:64px; height:64px; border-radius:12px; object-fit:cover;
  box-shadow: inset 0 0 0 1px rgba(15,23,42,.06);
}

/* 이름/배지 */
.name-container{ display:flex; align-items:center; gap:10px; flex-wrap:wrap; min-height:28px; }
.project-name{
  font-size:20px; font-weight:800; color:var(--text);
  border:none; outline:none; background:transparent;
  width:auto; min-width:140px; max-width:100%;
  border-bottom:2px solid transparent;
}
.project-name:focus{ border-bottom-color: rgba(37,99,235,.35); }
.pi-badge{
  font-size:12px; font-weight:700; color:#0b1224;
  padding:4px 10px; border-radius:999px;
  background:#eef2ff;
  border:1px solid rgba(15,23,42,.06);
}

/* 설명 */
.project-description{
  width:100%; min-height:78px;
  border:1px solid var(--line); border-radius:12px;
  padding:12px 14px; resize:none;
  background:#fff; color:var(--text);
}
.project-description::placeholder{ color:#9aa4b2 }

/* 팀원 뱃지 (풀네임) */
.team-list{ display:flex; flex-wrap:wrap; gap:8px; }
.member{
  background:#eef6ff;
  color:#2563eb;
  padding:6px 12px;
  border-radius:999px;
  font-size:13px;
  font-weight:600;
  border:1px solid rgba(37,99,235,.15);
}

/* 진행도 */
.progress-container{ width:100% }
.progress-top{ display:flex; justify-content:space-between; align-items:center; margin-bottom:8px; }
.progress-label{ font-size:13px; color:var(--muted) }
.progress-state{
  font-size:12px; padding:2px 8px; border-radius:999px;
  border:1px solid var(--line); color:#0f172a; background:#f8fafc;
}
.progress-state[data-state="good"]{ border-color: rgba(34,197,94,.3); background:#ecfdf5; color:#065f46; }
.progress-state[data-state="warn"]{ border-color: rgba(245,158,11,.35); background:#fffbeb; color:#92400e; }
.progress-state[data-state="risk"]{ border-color: rgba(239,68,68,.35); background:#fef2f2; color:#991b1b; }

.progress-bar{
  width:100%; height:10px; border-radius:999px; overflow:hidden;
  background:#f1f5f9; border:1px solid var(--line);
}
.progress-fill{
  height:100%;
  background: var(--brand);
  transition: width .3s ease;
}
</style>