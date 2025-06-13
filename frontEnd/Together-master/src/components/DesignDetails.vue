<template>
  <section class="planning-details">
    <!-- 상단 버튼 네비게이션 -->
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

    <!-- 편집 영역 -->
    <div class="editor-container">
      <Editor
        v-if="!readonly"
        v-model="activeItem.content"
        :init="editorConfig"
        :api-key="editorConfig.apiKey"
        :tinymce-script-src="`https://cdn.tiny.cloud/1/${editorConfig.apiKey}/tinymce/6/tinymce.min.js`"
        @update:modelValue="onContentChange"
      />
      <div v-else class="readonly-content" v-html="activeItem.content"></div>
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
            <img :src="toDrivePreview(file.url)" class="file-thumb" />
          </template>
          <template v-else>
            <div class="file-icon">📄</div>
          </template>
          <div class="file-info">
            <a
              :href="file.url"
              download
              :title="extractFileName(file.url)"
            >{{ extractFileName(file.url) }}</a>
            <span class="file-date">{{ formatDate(file.uploadedAt) }}</span>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import Editor from '@tinymce/tinymce-vue'

const router = useRouter()
const fileInputRef = ref(null)
const props = defineProps({ projectId: Number, readonly: Boolean })
const emit = defineEmits(['updateStepProgress'])

// 페이지 이동용 링크
const PAGE_LINKS = {
  usecase:      '/usecase-page',
  classDiagram: '/class-diagram',
  erd:          '/erd-diagram'
}

// 디자인 단계를 담은 배열
const designItems = reactive([
  { name: "유스케이스 다이어그램", type: "usecase",       content: "", files: [], completed: false },
  { name: "클래스 다이어그램",     type: "classDiagram",   content: "", files: [], completed: false },
  { name: "시퀀스 다이어그램",     type: "sequence",       content: "", files: [], completed: false },
  { name: "UI 디자인",            type: "ui",             content: "", files: [], completed: false },
  { name: "ERD",                  type: "erd",            content: "", files: [], completed: false },
  { name: "테이블 명세",           type: "table",          content: "", files: [], completed: false },
  { name: "시스템 아키텍쳐",       type: "architecture",   content: "", files: [], completed: false },
  { name: "프로젝트 일정 계획",   type: "schedule",       content: "", files: [], completed: false }
])

const selectedIndex = ref(0)
const activeItem = computed(() => designItems[selectedIndex.value])

// TinyMCE 설정 (PlanningDetails와 동일)
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
      input.type = 'file'
      input.accept = 'image/*'
      input.onchange = () => {
        const file = input.files[0]
        const reader = new FileReader()
        reader.onload = e => callback(e.target.result, { alt: file.name })
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

      const res = await axios.post('/design/upload', form, {
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

// 탭 클릭: 일부 타입은 페이지 이동
function selectTab(idx) {
  const type = designItems[idx].type
  if (['usecase', 'classDiagram', 'erd'].includes(type)) {
    router.push(PAGE_LINKS[type])
  } else {
    selectedIndex.value = idx
  }
}

// 완료 여부 체크 + 부모 컴포넌트에 업데이트
function markCompleted() {
  activeItem.value.completed =
    Boolean(activeItem.value.content.trim()) ||
    activeItem.value.files.length > 0
  emit('updateStepProgress',
    designItems.filter(i => i.completed).length
  )
}

// 구글 드라이브 링크를 미리보기 링크로 변환
function convertDownloadToView(html) {
  return html.replace(
    /https:\/\/drive\.google\.com\/uc\?export=download&id=([a-zA-Z0-9_-]+)/g,
    'https://drive.google.com/uc?export=view&id=$1'
  )
}

let saveTimeout
function onContentChange(val) {
  activeItem.value.text = val
  markCompleted()

  clearTimeout(saveTimeout)
  saveTimeout = setTimeout(async () => {
    const form = new FormData()
    form.append('type', activeItem.value.type)
    form.append('text', activeItem.value.text)             // ← key를 text로
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

// 파일 선택 / 드래그 앤 드롭 처리
function onFileSelect(e) {
  uploadFiles(Array.from(e.target.files))
}
function handleDrop(e) {
  uploadFiles(Array.from(e.dataTransfer.files))
}
function uploadFiles(files) {
  const form = new FormData()
  form.append('type', activeItem.value.type)
  form.append('projectId', props.projectId)
  files.forEach(f => form.append('files', f))

  axios.post('/design/upload', form, {
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

// 파일 삭제
async function removeFile(idx) {
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

// 초기 데이터 로드
onMounted(async () => {
  try {
    const res = await axios.get('/design/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    designItems.forEach(item => {
      const raw = res.data[item.type]?.json || res.data[item.type]?.text || ''
      item.content = convertDownloadToView(raw)
      item.files   = res.data[item.type]?.files || []
      item.completed =
        Boolean(item.content.trim()) || item.files.length > 0
    })
    emit('updateStepProgress',
      designItems.filter(i => i.completed).length
    )
  } catch (err) {
    console.error('데이터 로딩 오류', err)
  }
})

const extractFileName = url => url.split('/').pop()
const isImage = url =>
  /\.(jpe?g|png|gif|bmp|webp)$/i.test(url) ||
  url.includes('drive.google.com/')
const toDrivePreview = url => {
  if (url.includes('uc?export=download')) {
    return url.replace('export=download', 'export=view')
  }
  return url
}
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
</style>
