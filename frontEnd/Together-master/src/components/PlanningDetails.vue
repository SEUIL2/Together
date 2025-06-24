<template>
  <section class="planning-details">
    <!-- 상단 버튼 네비게이션 -->
    <div class="nav-buttons">
      <button
        v-for="(item, idx) in planningItems"
        :key="idx"
        :class="['nav-btn', { active: selectedIndex === idx, completed: item.completed }]"
        @click="selectTab(idx)"
      >
        {{ item.name }}
      </button>
    </div>

    <!-- 편집 영역 -->
    <div class="editor-container" @contextmenu.prevent="handleRightClick" style="position: relative">
      <template v-if="activeItem.type === 'storyboard'">
        <textarea
          v-model="activeItem.content"
          class="basic-textarea"
          :readonly="readonly"
          placeholder="스토리보드 또는 Figma 공유 링크 입력"
          @input="onContentChange($event.target.value)"
        />
        <iframe
          v-if="isValidFigmaLink(activeItem.content)"
          :src="convertToFigmaEmbed(extractFigmaUrl(activeItem.content))"
          width="100%"
          height="500"
          allowfullscreen
          style="margin-top: 12px; border: 1px solid #ccc; border-radius: 8px;"
        ></iframe>
      </template>
      <template v-else>
        <Editor
          v-if="!readonly"
          v-model="activeItem.content"
          :init="editorConfig"
          :api-key="editorConfig.apiKey"
          :tinymce-script-src="`https://cdn.tiny.cloud/1/${editorConfig.apiKey}/tinymce/6/tinymce.min.js`"
          @update:modelValue="onContentChange"
        />
        <div v-else class="readonly-content" v-html="activeItem.content"></div>
      </template>
    </div>

    <!-- 파일 및 이미지 업로드 -->
    <div class="upload-section">
      <div v-if="!readonly" class="upload-zone"
           @click="fileInputRef.click()"
           @dragover.prevent
           @drop.prevent="handleDrop">
        <input type="file" multiple ref="fileInputRef" @change="onFileSelect" hidden />
        <div class="upload-message">
          파일 또는 이미지를 드래그 혹은 클릭하여 업로드
        </div>
      </div>

      <div class="file-grid">
        <div v-for="(file, i) in activeItem.files" :key="i" class="file-card">
          <button v-if="!readonly" class="file-delete-btn" @click.stop="removeFile(i)">×</button>
          <template v-if="isImage(file.url)">
            <img :src="file.url" class="file-thumb" />
          </template>
          <template v-else>
            <div class="file-icon">📄</div>
          </template>
          <div class="file-info">
            <a :href="file.url" download :title="extractFileName(file.url)">
              {{ extractFileName(file.url) }}
            </a>
            <span class="file-date">{{ formatDate(file.uploadedAt) }}</span>
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
        :page="'planning-details'"
        :readonly="true"
        :projectId="resolvedProjectId"
        @close="showFeedbackInput = false"
        @submitted="() => { showFeedbackInput = false; loadFeedbacks() }"
      />
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import Editor from '@tinymce/tinymce-vue'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import { useFeedback } from '@/composables/useFeedback'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const fileInputRef = ref(null)
const props = defineProps({ projectId: Number, readonly: Boolean })
const resolvedProjectId = computed(() => props.projectId || Number(route.params.projectId))
const emit = defineEmits(['updateStepProgress'])

const feedbacks = ref([])
const showFeedbackInput = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const selectedFeedback = ref(null)
const { markFeedbackAsRead } = useFeedback()

function extractFigmaUrl(content) {
  if (!content) return ''
  const match = content.match(/https:\/\/www\.figma\.com\/(file|design)\/[^\s<"]+/)
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

function handleRightClick(e) {
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

async function loadFeedbacks() {
  try {
    const { data } = await axios.get('/feedbacks/project', {
      params: { page: 'planning-details', projectId: resolvedProjectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    feedbacks.value = data.filter(fb => !fb.isRead)
  } catch (err) {
    console.error('❌ 피드백 불러오기 실패:', err)
  }
}
const planningItems = reactive([
  { name: '프로젝트 동기', type: 'motivation', content: '', files: [], completed: false },
  { name: '프로젝트 목표', type: 'goal', content: '', files: [], completed: false },
  { name: '요구사항 정의', type: 'requirement', content: '', files: [], completed: false },
  { name: '정보구조도', type: 'infostructure', content: '', files: [], completed: false },
  { name: '스토리보드', type: 'storyboard', content: '', files: [], completed: false }
])
const PAGE_LINKS = {
  infostructure: '/info-structure',
}
const selectedIndex = ref(0)
const activeItem = computed(() => planningItems[selectedIndex.value])

const editorConfig = {
  apiKey: '96jqrzcetlm5lwov39n7p1j9urvurkwl8ya18w22y816w94l',
  height: 500,
  language: 'ko_KR',
  resize: false,
  menubar: false,
  branding: false,
  statusbar: false,
  paste_data_images: true,
  file_picker_types: 'image',
  plugins: 'lists link image table code autosave fullscreen',
  toolbar: 'undo redo | bold italic underline | bullist numlist | table | image | code | fullscreen',
  automatic_uploads: true,
    file_picker_callback: (callback, value, meta) => {
    if (meta.filetype === 'image') {
      const input = document.createElement('input')
      input.setAttribute('type', 'file')
      input.setAttribute('accept', 'image/*')
      input.onchange = () => {
        const file = input.files[0]
        const reader = new FileReader()
        reader.onload = (e) => {
          // base64로 읽은 결과를 img src로 바로 삽입
          callback(e.target.result, { alt: file.name })
        }
        reader.readAsDataURL(file)
      }
      input.click()
    }
  },
  images_upload_handler: async (blobInfo, success, failure) => {
    try {
      const form = new FormData()
      form.append('type', activeItem.value.type)
      form.append('projectId', props.projectId)
      form.append('files', blobInfo.blob(), blobInfo.filename())
      const res = await axios.post('/planning/upload', form, {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
      const url = res.data.files[0].url
      success(url)
      activeItem.value.files.push({ url, uploadedAt: new Date().toISOString() })
      markCompleted()
    } catch {
      failure('Upload error')
    }
  }
}

function selectTab(idx) {
  const type = planningItems[idx].type
  // 만약 정보구조도(또는 다른 다이어그램)라면 페이지 이동
  if (type === 'infostructure') {
    router.push({
      path: `/info-structure/${resolvedProjectId.value}`,
      query: {
        readonly: props.readonly ?? route.query.readonly === 'true',
        projectTitle: route.query.projectTitle || '프로젝트'
      }
    })
  } else {
    selectedIndex.value = idx
  }
}


function markCompleted() {
  activeItem.value.completed = Boolean(activeItem.value.content.trim()) || activeItem.value.files.length > 0
  emit('updateStepProgress', planningItems.filter(i => i.completed).length)
}
function stripHtmlTags(html) {
  return html
    .replace(/<br\s*\/?>/gi, '\n')
    .replace(/<\/p>/gi, '\n')
    .replace(/<[^>]+>/g, '')   // 모든 태그 제거
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .trim();
}
let saveTimeout
function onContentChange(val) {
  activeItem.value.content = val // 에디터에는 여전히 html 유지
  markCompleted()
  clearTimeout(saveTimeout)
  saveTimeout = setTimeout(async () => {
    const form = new FormData()
    form.append('type', activeItem.value.type)
    // 여기만 stripHtmlTags로 변경
    form.append('text', stripHtmlTags(activeItem.value.content))
    form.append('projectId', props.projectId)
    try {
      await axios.put('/planning/update', form, {
        headers: { Authorization: localStorage.getItem('authHeader') },
        withCredentials: true
      })
    } catch (err) {
      console.error('자동 저장 실패', err)
    }
  }, 800)
}

function onFileSelect(e) {
  const files = Array.from(e.target.files)
  uploadFiles(files)
}

function handleDrop(e) {
  const files = Array.from(e.dataTransfer.files)
  uploadFiles(files)
}

function uploadFiles(files) {
  const form = new FormData()
  form.append('type', activeItem.value.type)
  form.append('projectId', props.projectId)
  files.forEach(f => form.append('files', f))
  axios.post('/planning/upload', form, {
    headers: { Authorization: localStorage.getItem('authHeader') },
    withCredentials: true
  })
    .then(res => {
      activeItem.value.files.push(...res.data.files)
      markCompleted()
    })
    .catch(err => {
      console.error('파일 업로드 오류', err)
      alert('파일 업로드에 실패했습니다.')
    })
}

async function removeFile(idx) {
  const file = activeItem.value.files[idx]
  try {
    await axios.delete('/planning/delete-file', {
      params: {           // ← 반드시 params!
        type: activeItem.value.type,
        fileUrl: file.url
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
    // 기존 planning 데이터 불러오기
    const res = await axios.get('/planning/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    planningItems.forEach(item => {
      item.content = res.data[item.type]?.text || ''
      item.files = res.data[item.type]?.files || []
      item.completed = Boolean(item.content.trim()) || item.files.length > 0
    })
    emit('updateStepProgress', planningItems.filter(i => i.completed).length)

    // 🔥 여기서 피드백도 불러오기
    await loadFeedbacks()

  } catch (err) {
    console.error('데이터 로딩 오류', err)
  }
})


const extractFileName = url => url.split('/').pop()
const isImage = url => /\.(jpe?g|png|gif|bmp|webp)$/i.test(url)
const formatDate = date => new Date(date).toLocaleString()
</script>

<style scoped>
.planning-details { display: flex; flex-direction: column; gap: 10px; }
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
.feedback-marker {
  font-size: 20px;
  cursor: pointer;
}
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
</style>
