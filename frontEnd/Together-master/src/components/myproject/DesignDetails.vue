<template>
  <section
    class="planning-details"
    @contextmenu.prevent="handleRightClick"
    style="position: relative"
  >
    <!-- 🔹 상단 탭 버튼 -->
    <div class="nav-buttons">
      <button
        v-for="(item, idx) in designItems"
        :key="idx"
        :class="['nav-btn', { active: selectedIndex === idx, completed: item.completed }]"
        @click="selectTab(idx)"
      >
        {{ item.name }}
      </button>
    </div>
<!-- 여기 추가! -->
    <div v-if="!activeItem" class="empty-message">
      <div class="empty-inner">
        <span class="empty-icon">🗂️</span>
        <p>상단 탭에서 원하는 항목을 클릭하세요!<br>유스케이스 다이어그램, 클래스 다이어그램, ERD, 프로젝트 일정은 페이지가 이동 됩니다.</p>
      </div>
    </div>
    <!-- 🔹 에디터 영역 -->
    <div class="editor-container" v-if="activeItem">
      <!-- 🔸 UI 디자인 -->
      <template v-if="activeItem.type === 'ui'">
        <textarea
          v-model="activeItem.content"
          class="basic-textarea"
          :readonly="readonly"
          placeholder="Figma 링크 또는 UI 설명 입력"
          @input="onContentChange($event.target.value)"
        ></textarea>

        <iframe
          v-if="isValidFigmaLink(activeItem.content)"
          :src="convertToFigmaEmbed(extractFigmaUrl(activeItem.content))"
          width="100%"
          height="500"
          allowfullscreen
          style="margin-top: 12px; border: 1px solid #ccc; border-radius: 8px;"
        ></iframe>
      </template>

      <!-- 🔸 시퀀스 다이어그램 -->
      <template v-else-if="activeItem.type === 'sequence'">
        <textarea
          v-model="activeItem.content"
          class="basic-textarea"
          :readonly="readonly"
          placeholder="draw.io 공유 링크 또는 설명 입력"
          @input="onContentChange($event.target.value)"
        ></textarea>

        <iframe
          v-if="isValidDrawioLink(activeItem.content)"
          :src="convertToDrawioEmbed(activeItem.content)"
          width="100%"
          height="500"
          frameborder="0"
          allowfullscreen
          style="margin-top: 12px; border: 1px solid #ccc; border-radius: 8px;"
        ></iframe>
      </template>
    </div>

    <!-- 🔹 파일 업로드 영역 -->
    <div class="upload-section" v-if="activeItem">
      <div
        v-if="!readonly"
        class="upload-zone"
        @click="fileInputRef.click()"
        @dragover.prevent
        @drop.prevent="handleDrop"
      >
        <input type="file" multiple ref="fileInputRef" @change="onFileSelect" hidden />
        <div class="upload-message">파일 또는 이미지를 드래그하거나 클릭해 업로드하세요</div>
      </div>

      <div class="file-grid">
        <div v-for="(file, i) in activeItem.files" :key="i" class="file-card">
          <button v-if="!readonly" class="file-delete-btn" @click.stop="removeFile(i)">×</button>
          <template v-if="isImage(file.url)">
            <img :src="toDrivePreview(file.url)" class="file-thumb" />
          </template>
          <template v-else>
            <div class="file-icon-wrapper">
              <div class="file-icon">📄</div>
            </div>
          </template>
          <div class="file-info">
            <a :href="file.url" download :title="extractFileName(file.url)">
              {{ extractFileName(file.url) }}
            </a>
            <span class="file-date">{{ formatDate(file.uploadedAt) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 피드백 마커 -->
    <div
      v-for="fb in feedbacks"
      :key="fb.feedbackId"
      class="feedback-marker"
      :style="{ top: fb.y + 'px', left: fb.x + 'px', position: 'absolute' }"
      @click="selectedFeedback = fb"
    >
      📌
    </div>

    <!-- 피드백 팝업 -->
    <FeedbackPopup
      v-if="selectedFeedback"
      :fb="selectedFeedback"
      :readonly="true"
      @read="handleReadFeedback"
      @close="selectedFeedback = null"
    />

    <!-- 피드백 입력창 (교수 전용) -->
    <FeedbackInput
      v-if="showFeedbackInput"
      :x="feedbackPosition.x"
      :y="feedbackPosition.y"
      :page="`design-${activeItem.type}`"
      :readonly="true"
      :projectId="props.projectId"
      @close="showFeedbackInput = false"
      @submitted="() => { showFeedbackInput = false; loadFeedbacks(`design-${activeItem.type}`) }"
    />
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import axios from 'axios'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import { useFeedback } from '@/composables/useFeedback.js'

const router = useRouter()
const route = useRoute()
const fileInputRef = ref(null)
const props = defineProps({ projectId: Number, readonly: Boolean, projectTitle: String })
const emit = defineEmits(['updateStepProgress'])

const feedbacks = ref([])
const showFeedbackInput = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const selectedFeedback = ref(null)
const { markFeedbackAsRead } = useFeedback()

const designItems = reactive([
  { name: "유스케이스 다이어그램", type: "usecase", content: "", files: [], completed: false },
  { name: "클래스 다이어그램", type: "classDiagram", content: "", files: [], completed: false },
  { name: "시퀀스 다이어그램", type: "sequence", content: "", files: [], completed: false },
  { name: "UI 디자인", type: "ui", content: "", files: [], completed: false },
  { name: "ERD", type: "erd", content: "", files: [], completed: false },
  { name: "테이블 명세", type: "table", content: "", files: [], completed: false },
  { name: "시스템 아키텍쳐", type: "architecture", content: "", files: [], completed: false },
  { name: "프로젝트 일정", type: "schedule", content: "", files: [], completed: false }
])

const PAGE_LINKS = {
  usecase: '/usecase-diagram',
  classDiagram: '/class-diagram',
  erd: '/erd-diagram',
  schedule: '/Scheduletest'
}

const selectedIndex = ref(null)
const activeItem = computed(() => selectedIndex.value !== null ? designItems[selectedIndex.value] : null)

function handleRightClick(e) {
  if (!props.readonly || !activeItem.value) return
  const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop

  feedbackPosition.value = {
    x: e.clientX + scrollLeft,
    y: e.clientY + scrollTop
  }

  showFeedbackInput.value = true
}

function handleReadFeedback(id) {
  markFeedbackAsRead(id)
  feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== id)
  selectedFeedback.value = null
}

async function loadFeedbacks(pageIdentifier) {
  if (!pageIdentifier || !props.projectId) return;
  try {
    const { data } = await axios.get('/feedbacks/project', {
      params: { page: pageIdentifier, projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    feedbacks.value = data.filter(fb => !fb.isRead)
  } catch (err) { console.error('❌ 피드백 불러오기 실패:', err); feedbacks.value = []; }
}

function extractFigmaUrl(content) {
  const parser = new DOMParser()
  const doc = parser.parseFromString(content, 'text/html')
  const text = doc.body.textContent || ''
  const match = text.match(/https:\/\/www\.figma\.com\/(file|design)\/[^\s<"]+/)
  return match ? match[0] : ''
}

function convertToFigmaEmbed(link) {
  if (!link) return ''
  try {
    const url = new URL(link)
    const segments = url.pathname.split('/')
    const fileKey = segments[2]
    const fileName = segments[3] || 'Untitled'
    const nodeId = url.searchParams.get('node-id') || '0%3A1'
    return `https://www.figma.com/embed?embed_host=share&url=https://www.figma.com/file/${fileKey}/${fileName}?type=design&node-id=${encodeURIComponent(nodeId)}`
  } catch {
    return ''
  }
}

function isValidFigmaLink(content) {
  return extractFigmaUrl(content) !== ''
}

function isValidDrawioLink(content) {
  return typeof content === 'string' && content.includes('viewer.diagrams.net')
}

function convertToDrawioEmbed(link) {
  try {
    if (!link) return ''
    if (link.includes('viewer.diagrams.net')) return link
    const match = link.match(/#G([a-zA-Z0-9_-]+)/)
    if (match && match[1]) {
      const fileId = match[1]
      const driveUrl = `https://drive.google.com/uc?id=${fileId}`
      return `https://viewer.diagrams.net/?highlight=0000ff&edit=_blank&layers=1&nav=1#U${encodeURIComponent(driveUrl)}`
    }
    return ''
  } catch {
    return ''
  }
}

function selectTab(idx) {
  const type = designItems[idx].type
  if (type === 'schedule') {
    // 프로젝트 일정은 무조건 고정 경로 이동 (파라미터 없이)
    router.push('/Scheduletest')
    return
  }
  if (['usecase', 'classDiagram', 'erd'].includes(type)) {
    router.push({
      path: `${PAGE_LINKS[type]}/${props.projectId}`,
      query: {
        readonly: props.readonly ?? route.query.readonly === 'true',
        projectTitle: props.projectTitle || route.query.projectTitle || '프로젝트'
      }
    })
  } else {
    selectedIndex.value = idx
    loadFeedbacks(`design-${designItems[idx].type}`);
  }
}

watch([() => route.query.substep, () => props.projectId], ([newSubstep, newProjectId]) => {
  if (newSubstep && newProjectId) {
    const index = designItems.findIndex(item => item.type === newSubstep);
    if (index !== -1) {
      // selectTab 함수를 호출하여 페이지 이동 로직까지 처리
      selectTab(index);
    }
  }
}, { immediate: true });


function markCompleted() {
  if (!activeItem.value) return
  const content = activeItem.value.content
  activeItem.value.completed = (typeof content === 'string' && content.trim() !== '') || activeItem.value.files.length > 0
  emit('updateStepProgress', designItems.filter(i => i.completed).length)
}

let saveTimeout
function onContentChange(val) {
  if (!activeItem.value) return
  if (val !== undefined) activeItem.value.content = val
  markCompleted()
  clearTimeout(saveTimeout)
  saveTimeout = setTimeout(async () => {
    const form = new FormData()
    form.append('type', activeItem.value.type)
    form.append('text', activeItem.value.content)
    form.append('projectId', props.projectId)
    try {
      await axios.put('/design/update', form, {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
    } catch (err) {
      console.error('자동 저장 실패', err)
    }
  }, 800)
}

function onFileSelect(e) {
  uploadFiles(Array.from(e.target.files))
}
function handleDrop(e) {
  uploadFiles(Array.from(e.dataTransfer.files))
}
function uploadFiles(files) {
  if (!activeItem.value) return
  const form = new FormData()
  form.append('type', activeItem.value.type)
  form.append('projectId', props.projectId)
  files.forEach(f => form.append('files', f))
  axios.post('/design/upload', form, {
    headers: { Authorization: localStorage.getItem('authHeader') },
    withCredentials: true
  }).then(res => {
    activeItem.value.files.push(...res.data.files)
    markCompleted()
  }).catch(err => {
    console.error('파일 업로드 오류', err)
    alert('파일 업로드에 실패했습니다.')
  })
}

async function removeFile(idx) {
  if (!activeItem.value) return
  const file = activeItem.value.files[idx]
  try {
    await axios.delete('/design/delete-file', {
      params: {
        type: activeItem.value.type,
        fileUrl: file.url,
        projectId: props.projectId
      },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    activeItem.value.files.splice(idx, 1)
    markCompleted()
  } catch (err) {
    console.error('파일 삭제 오류', err)
    alert('파일 삭제에 실패했습니다.')
  }
}

onMounted(async () => {
  try {
    const res = await axios.get('/design/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    designItems.forEach(item => {
      const rawData = res.data[item.type]
      const useTextOnly = ['ui', 'sequence', 'table', 'architecture']
      item.content = useTextOnly.includes(item.type)
        ? rawData?.text || ''
        : rawData?.json || ''
      item.files = rawData?.files || []
      item.completed = (item.content && item.content.trim() !== '') || item.files.length > 0
    })

    emit('updateStepProgress', designItems.filter(i => i.completed).length)

    if (activeItem.value) {
      await loadFeedbacks(`design-${activeItem.value.type}`);
    }
  } catch (err) {
    console.error('데이터 로딩 오류', err)
  }
})

const extractFileName = url => url.split('/').pop()
const isImage = url => /\.(jpe?g|png|gif|bmp|webp)$/i.test(url) || url.includes('drive.google.com/')
const toDrivePreview = (url) => {
  if (!url) return '';
  const fileIdMatch = url.match(/file\/d\/([a-zA-Z0-9_-]+)/);
  if (fileIdMatch && fileIdMatch[1]) {
    return `https://drive.google.com/uc?export=view&id=${fileIdMatch[1]}`;
  }
  if (url.includes('uc?export=download')) {
    return url.replace('export=download', 'export=view');
  }
  return url;
};
const formatDate = date => new Date(date).toLocaleString()
</script>

<style scoped>
.planning-details { display: flex; flex-direction: column; gap: 10px; position: relative; }
.nav-buttons { display: flex; gap: 8px; }
.nav-btn { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: 1px solid #ccc; border-radius: 12px; background: #fff; cursor: pointer; transition: background .2s, border-color .2s; }
.nav-dot { width: 8px; height: 8px; border: 2px solid #4a90e2; border-radius: 50%; }
.nav-dot.filled, .nav-btn.active .nav-dot, .nav-dot.selected { background: #4a90e2; }
.nav-btn.active { background: #4a90e2; color: #fff; border-color: #4a90e2; }
.nav-btn.completed:not(.active) { border-color: #4a90e2; }
.editor-container { min-height: 320px; }

.upload-section { display: flex; flex-direction: column; gap: 0px; }
.upload-zone { border: 2px dashed #4a90e2; border-radius: 8px; padding: 20px; text-align: center; color: #4a90e2; font-size: 14px; cursor: pointer; height: 60px;}
.upload-zone:hover { background: rgba(74, 144, 226, 0.1); }
.file-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(120px, 1fr)); gap: 16px; }
.file-card { position: relative; background: #fff; border: 1px solid #eee; border-radius: 8px; padding: 8px; text-align: center; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
.file-delete-btn { position: absolute; top: 4px; right: 4px; background: #ff5c5c; color: #fff; border: none; border-radius: 50%; width: 24px; height: 24px; cursor: pointer; font-size: 14px; line-height: 24px; padding: 0; }
.file-thumb { width: 100%; height: 80px; object-fit: cover; border-radius: 4px; margin-bottom: 8px; }
.file-icon { font-size: 48px; color: #ccc; margin-bottom: 8px; }
.file-icon-wrapper {
  width: 100%;
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.file-icon { font-size: 48px; color: #ccc; }
.file-info { font-size: 12px; color: #333; display: flex; flex-direction: column; gap: 4px; }
.file-info a {
  display: block;
  width: 100%;
  max-width: 140px;         /* 카드 너비에 맞게 조정 */
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: #4a90e2;
  text-decoration: none;
  cursor: pointer;
}
.file-date { font-size: 10px; color: #999; }
.readonly-content { padding: 12px; border: 1px solid #eee; border-radius: 8px; background: #f9f9f9; min-height: 200px; }
.basic-textarea {
  width: 100%;
  height: 50px;
  padding: 12px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 8px;
  resize: none;
  white-space: pre-wrap;
}
.empty-message {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 430px;
}
.empty-inner {
  text-align: center;
  color: #aaa;
  font-size: 1.18rem;
  line-height: 1.8;
  padding: 20px 32px;
  border-radius: 18px;
  background: #f7f7f9;
  border: 1.5px solid #eee;
}
.empty-icon {
  font-size: 2.3rem;
  display: block;
  margin-bottom: 10px;
}

.feedback-marker {
  font-size: 20px;
  cursor: pointer;
}

</style>
