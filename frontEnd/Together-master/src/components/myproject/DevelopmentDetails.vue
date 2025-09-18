<template>
  <section class="planning-details">
    <!-- 상단 버튼 네비게이션 -->
    <div class="nav-buttons">
      <button
        v-for="(item, idx) in devItems"
        :key="idx"
        :class="['nav-btn', { active: selectedIndex === idx, completed: item.completed }]"
        @click="selectTab(idx)"
      >
        {{ item.name }}
      </button>
    </div>

    <template v-if="activeItem.type === 'devOrder'">
      <DevOrderTable :project-id="projectId" />
    </template>

    <template v-else>
      <!-- 에디터/읽기전용 -->
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
              <img :src="toDrivePreview(file.url)" class="file-thumb" @click="openImageModal(file.url)" />
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
    </template>

    <!-- 이미지 미리보기 모달 -->
    <div class="modal-overlay" v-if="showModal" @click="closeModal">
      <div class="modal-content" @click.stop>
        <img :src="modalImageSrc" class="modal-image" />
        <button class="modal-close" @click="closeModal">&times;</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import Editor from '@tinymce/tinymce-vue'
import DevOrderTable from './DevOrderTable.vue'
import axios from 'axios'
import { useRoute } from 'vue-router'

const fileInputRef = ref(null)
const props = defineProps({ projectId: Number, readonly: Boolean, substep: String })
const emit = defineEmits(['updateStepProgress'])

// 타입 이름을 백엔드 엔티티 필드명에 맞춤
const devItems = reactive([
  { name: '개발 환경 설정', type: 'environment', content: '', files: [], completed: false },
  { name: '기능별 개발 순서', type: 'devOrder', content: '', files: [], completed: false, rows: [] },
  { name: '커밋 메시지 규칙', type: 'commitRule', content: '', files: [], completed: false },
  { name: '폴더 구조 및 파일 규칙', type: 'folder', content: '', files: [], completed: false }
])

const route = useRoute()
const selectedIndex = ref(0)
const activeItem = computed(() => devItems[selectedIndex.value])

const showModal = ref(false)
const modalImageSrc = ref("")

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

      const res = await axios.post('/develop/upload', form, {
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
  selectedIndex.value = idx
}

import { watch } from 'vue'

watch(() => route.query.substep, (newSubstep) => {
  if (newSubstep) {
    const index = devItems.findIndex(item => item.type === newSubstep);
    if (index !== -1) {
      selectTab(index);
    }
  }
}, { immediate: true });


function markCompleted() {
  activeItem.value.completed =
    Boolean(activeItem.value.content.trim()) ||
    activeItem.value.files.length > 0
  emit('updateStepProgress',
    devItems.filter(i => i.completed).length
  )
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
  // **빈 문자열도 content에 기록**
  activeItem.value.content = val
  markCompleted()

  clearTimeout(saveTimeout)
  saveTimeout = setTimeout(async () => {
    const form = new FormData()
    form.append('type', activeItem.value.type)
    // **항상 text 필드에 content를 담아서 보냄**
    form.append('text', stripHtmlTags(activeItem.value.content))
    form.append('projectId', props.projectId)

    try {
      await axios.put('/develop/update', form, {
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
  const form = new FormData()
  form.append('type', activeItem.value.type)
  form.append('projectId', props.projectId)
  files.forEach(f => form.append('files', f))

  axios.post('/develop/upload', form, {
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
    await axios.delete('/develop/delete-file', {
      params: {
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
    const res = await axios.get('/develop/all', {
      params: { projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })

    devItems.forEach(item => {
      if (item.type === 'devOrder') return;
      // 백엔드에서 json 필드는 없으므로 text나 json 모두 담기
      let html = res.data[item.type]?.json || res.data[item.type]?.text || ''
      item.content = convertDownloadToView(html)
      item.files   = res.data[item.type]?.files || []
      item.completed =
        Boolean(item.content.trim()) || item.files.length > 0
    })
    emit('updateStepProgress', devItems.filter(i => i.completed).length)

    // '기능별 개발 순서' 탭 데이터 로드
    // await fetchDevOrderItems(); // DevOrderTable 컴포넌트에서 자체적으로 로드
  } catch (err) {
    console.error('데이터 로딩 오류', err)
  }
})

function convertDownloadToView(html) {
  return html.replace(
    /https:\/\/drive\.google\.com\/uc\?export=download&id=([a-zA-Z0-9_-]+)/g,
    'https://drive.google.com/uc?export=view&id=$1'
  )
}
const extractFileName = url => url.split('/').pop()
const isImage = url =>
  /\.(jpe?g|png|gif|bmp|webp)$/i.test(url) ||
  url.includes('drive.google.com/')
const toDrivePreview = (url) => {
  if (!url) return '';
  const fileIdMatch = url.match(/file\/d\/([a-zA-Z0-9_-]+)/);
  if (fileIdMatch && fileIdMatch[1]) {
    return `https://drive.google.com/uc?export=view&id=${fileIdMatch[1]}`;
  }
  return url.includes('uc?export=download') ? url.replace('export=download', 'export=view') : url;
};
function openImageModal(url) {
  modalImageSrc.value = url
  showModal.value = true
}
function closeModal() {
  showModal.value = false
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
<style scoped>
.dev-order-container {
  margin-top: 16px;
}
.dev-order-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}
.dev-order-table th, .dev-order-table td {
  border: 1px solid #e0e0e0;
  padding: 8px 12px;
  text-align: left;
  vertical-align: middle;
}
.dev-order-table th {
  background-color: #f7f9fc;
  font-weight: 600;
}
.dev-order-table input[type="text"],
.dev-order-table input[type="number"],
.dev-order-table select {
  width: 100%;
  border: 1px solid transparent;
  padding: 6px;
  border-radius: 4px;
  background-color: transparent;
  transition: border-color 0.2s, background-color 0.2s;
}
.dev-order-table input:focus, .dev-order-table select:focus {
  outline: none;
  border-color: #4a90e2;
  background-color: #fff;
}
.dev-order-table input[type="checkbox"] {
  width: 16px;
  height: 16px;
}
.text-center {
  text-align: center;
}
.delete-row-btn {
  background: none;
  border: none;
  color: #e53935;
  cursor: pointer;
  font-size: 18px;
  font-weight: bold;
}
.add-row-btn {
  margin-top: 12px;
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #4a90e2;
  background-color: #eef6ff;
  color: #4a90e2;
  cursor: pointer;
  font-weight: 600;
}
</style>
