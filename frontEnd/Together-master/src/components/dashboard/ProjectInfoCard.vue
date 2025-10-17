<template>
  <div class="pi-card">
    <div v-if="saveStatus === 'saving'" class="save-toast saving">저장 중...</div>
    <div v-else-if="saveStatus === 'saved'" class="save-toast saved">💾 저장 완료</div>
    <div v-else-if="saveStatus === 'error'" class="save-toast error">저장 실패!</div>

    <!-- 숨김 파일 입력 -->
    <input
      type="file"
      ref="fileInput"
      accept="image/*"
      style="display:none"
      @change="handleImageChange"
    />

    <!-- 로고 -->
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

    <!-- 이름 -->
    <div class="name-container">
        <input
          v-model="projectName"
          ref="nameRef"
          class="project-name"
          placeholder="프로젝트 이름"
          :readonly="isReadOnly"
        />
    </div>

    <!-- 설명 -->
    <textarea
      v-model="projectDescription"
      ref="descRef"
      class="project-description"
      placeholder="프로젝트 설명을 입력하세요"
      :readonly="isReadOnly"
    ></textarea>

    <!-- 팀원 -->
    <div class="team-section">
      <h4 class="team-title">팀원</h4>
      <div class="team-list">
        <span class="member" v-for="member in teamMembers" :key="member.id || member.name">
          {{ member.name }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import api from '@/api'
import { debounce } from 'lodash'
import defaultLogo from '@/assets/togetherlogo.png'

const emit = defineEmits(['project-updated'])

const props = defineProps({
  projectId: { type: [String, Number], default: null },
  projectName: { type: String, default: '' },
  projectDescription: { type: String, default: '' },
  teamMembers: { type: Array, default: () => [] }, // [{id, name}]
  projectImageUrl: { type: String, default: '' },
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
    const { data } = await api.put('/projects/image', formData, {
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

const saveStatus = ref('idle'); // 'idle', 'saving', 'saved', 'error'
const autoSaveProjectInfo = debounce(async () => {
  if (!props.projectId || props.isReadOnly) return
  saveStatus.value = 'saving';
  try {
    await api.put(
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
    formData.append('projectId', props.projectId)
    await api.put('/planning/update', formData, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    emit('project-updated', {
      title: projectName.value,
      description: projectDescription.value
    })
    saveStatus.value = 'saved';
    setTimeout(() => saveStatus.value = 'idle', 2000);
  } catch (err) {
    console.error('❌ 자동 저장 실패:', err)
    saveStatus.value = 'error';
    setTimeout(() => saveStatus.value = 'idle', 3000);
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
  position: relative; /* 저장 알림 위치 기준점 */
  display:flex; flex-direction:column; align-items:center; gap:16px;
  padding:24px;
  background: var(--bg);
  border:1px solid var(--line);
  border-radius:14px;
  box-shadow: var(--shadow);
}

/* 로고 */
.logo-wrapper{
  width:100px; height:100px; border-radius:50%;
  display:flex; align-items:center; justify-content:center;
  background:#f8fafc;
  border:1px solid var(--line);
  cursor:pointer;
  transition: transform .15s ease, box-shadow .15s ease;
  padding: 0; /* 버튼 기본 패딩 제거 */
}
.logo-wrapper:hover:not(:disabled){
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(2,6,23,.08);
}
.project-logo{
  width:100%; height:100%; border-radius:50%; object-fit:cover;
  box-shadow: inset 0 0 0 1px rgba(15,23,42,.06);
}

/* 이름 */
.name-container{ text-align:center; }
.project-name{
  font-size:24px; font-weight:800; color:var(--text);
  border:none; outline:none; background:transparent;
  width:100%;
  border-bottom:2px solid transparent;
  text-align: center;
  padding-bottom: 4px;
}
.project-name:focus{ border-bottom-color: rgba(37,99,235,.35); }

/* 설명 */
.project-description{
  width:100%; min-height:80px;
  border:1px solid var(--line); border-radius:12px;
  padding:12px 14px; resize:none;
  background:#f8fafc; color:var(--text);
  text-align: center;
  font-size: 14px;
}
.project-description::placeholder{ color:#9aa4b2 }

/* 팀원 */
.team-section {
  width: 100%;
  text-align: center;
}
.team-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--muted);
  margin: 0 0 10px 0;
}
.team-list{ display:flex; flex-wrap:wrap; gap:8px; justify-content: center; }
.member{
  background:#eef6ff;
  color:#2563eb;
  padding:6px 12px;
  border-radius:999px;
  font-size:13px;
  font-weight:600;
  border:1px solid rgba(37,99,235,.15);
}

.save-toast {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
  background: #333;
  color: #fff;
  border-radius: 8px;
  font-size: 13px;
  padding: 8px 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: opacity .3s, transform .3s;
  pointer-events: none;
}
.save-toast.saved { background: #2563eb; }
.save-toast.error { background: #dc3545; }
</style>