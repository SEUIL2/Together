<template>
  <section class="planning-details" @contextmenu.prevent="handleRightClick">
    <div class="section-header">
      <!-- 상단 버튼 네비게이션 -->
      <div class="nav-buttons">
        <button
          v-for="(item, idx) in devItems"
          :key="idx"
          :class="['nav-btn', { active: selectedIndex === idx, completed: item.completed }]"
          @click="selectTab(idx)"
        >
          <svg v-if="item.completed" class="check-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
          {{ item.name }}
        </button>
      </div>
       <button @click="openScheduleModal" class="schedule-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
        개발 일정 보기
      </button>
    </div>

    <template v-if="activeItem.type === 'devOrder'">
      <DevOrderTable :project-id="projectId" />
    </template>

    <template v-else>
      <div class="content-grid">
        <!-- 왼쪽: 편집 영역 -->
        <div class="editor-wrapper">
          <div class="editor-container">
            <!-- 수정/저장/취소 버튼 -->
            <div class="edit-controls" v-if="!readonly">
              <button v-if="!isEditing" @click="startEditing" class="control-btn edit-btn">수정</button>
              <template v-else>
                <button @click="saveChanges" class="control-btn save-btn">저장</button>
                <button @click="cancelEditing" class="control-btn cancel-btn">취소</button>
              </template>
            </div>

            <!-- 내용 표시 영역 -->
            <textarea v-if="isEditing && !readonly" v-model="activeItem.content" class="basic-textarea large" placeholder="내용을 입력하세요..." @input="onContentChange($event.target.value)"></textarea>
            <div v-else class="readonly-content" :class="{ empty: !activeItem.content }" v-html="activeItem.content || '내용이 없습니다.'"></div>
            <div v-if="isEditing" class="char-counter">{{ (activeItem.content || '').length }} / 2000</div>
          </div>
        </div>

        <!-- 오른쪽: 파일 및 이미지 업로드 -->
        <div class="upload-section">
          <h4 class="section-title">첨부 파일</h4>
          <div class="attachments-container">
            <div class="file-grid">
              <div v-if="!readonly" class="upload-zone" @click="fileInputRef.click()" @dragover.prevent @drop.prevent="handleDrop">
                <input type="file" multiple ref="fileInputRef" @change="onFileSelect" hidden />
                <div class="upload-message">
                  <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
                  <span>파일 업로드</span>
                </div>
              </div>
              <div v-for="(file, i) in activeItem.files" :key="i" class="file-card">
                <button v-if="!readonly" class="file-delete-btn" @click.stop="removeFile(i)">
                  <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
                </button>
                <div class="file-preview">
                  <img v-if="isImage(file.url)" :src="toDrivePreview(file.url)" class="file-thumb" @click="openImageModal(file.url)" />
                  <div v-else class="file-icon">
                    <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                  </div>
                </div>
                <div class="file-info">
                  <a class="file-name" :href="file.url" download :title="extractFileName(file.url)">{{ extractFileName(file.url) }}</a>
                  <span class="file-date">{{ formatDate(file.uploadedAt) }}</span>
                </div>
              </div>
            </div>
            <div v-if="activeItem.files.length === 0" class="empty-files">
              첨부된 파일이 없습니다.
            </div>
          </div>
        </div>
      </div>
    </template>

    <!-- 카테고리별 일정 모달 -->
    <CategoryScheduleModal
      v-if="showScheduleModal"
      :project-id="props.projectId"
      :readonly="props.readonly"
      category="DEVELOPMENT"
      @close="showScheduleModal = false"
    />

    <!-- 컨텍스트 메뉴 (교수 전용) -->
    <ContextMenu
      v-if="showContextMenu"
      :x="feedbackPosition.x"
      :y="feedbackPosition.y"
      :visible="showContextMenu"
      @select="handleMenuSelect"
      @close="showContextMenu = false"
    />

    <!-- 피드백 관련 컴포넌트들 -->
    
    <FeedbackNote
      v-for="fb in feedbacks"
      :key="fb.feedbackId"
      :x="fb.x"
      :y="fb.y"
      :feedbackId="fb.feedbackId"
      :readonly="true"
      :category="fb.categories?.[0]?.name || ''"
      @click="selectedFeedback = fb"
    />
    <FeedbackPopup v-if="selectedFeedback" :fb="selectedFeedback" :readonly="true" @read="handleReadFeedback" @close="selectedFeedback = null" />
    <FeedbackInput v-if="showFeedbackInput" :x="feedbackPosition.x" :y="feedbackPosition.y" :page="`develop-${activeItem.type}`" :readonly="true" :projectId="props.projectId" @close="showFeedbackInput = false" @submitted="() => { showFeedbackInput = false; loadFeedbacks() }" />
  </section>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import CategoryScheduleModal from './CategoryScheduleModal.vue'
import DevOrderTable from './DevOrderTable.vue'
import axios from 'axios'
import { useRoute } from 'vue-router'
import ContextMenu from '@/components/feedback/ContextMenu.vue'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import FeedbackNote from '@/components/feedback/FeedbackNote.vue'
import { useFeedback } from '@/composables/useFeedback.js'

const isEditing = ref(false);
const originalContent = ref('');
const originalFiles = ref([]);

const feedbacks = ref([])
const showFeedbackInput = ref(false)
const showContextMenu = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const selectedFeedback = ref(null)
const { markFeedbackAsRead } = useFeedback()

function handleRightClick(e) {
  if (!props.readonly) return
  const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop
  feedbackPosition.value = { x: e.clientX + scrollLeft, y: e.clientY + scrollTop }
  showContextMenu.value = true
}

function handleMenuSelect(action) {
  if (action === 'add-feedback') {
    showFeedbackInput.value = true
  }
}

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

const router = useRouter()
const route = useRoute()
const selectedIndex = ref(0)
const activeItem = computed(() => devItems[selectedIndex.value])
const showScheduleModal = ref(false);

const openScheduleModal = () => {
  showScheduleModal.value = true;
};

function selectTab(idx) {
  selectedIndex.value = idx
  isEditing.value = false;
  loadFeedbacks();
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

function onContentChange(val) {
  if (isEditing.value) {
    activeItem.value.content = val;
    markCompleted();
  }
}

function startEditing() {
  isEditing.value = true;
  originalContent.value = activeItem.value.content;
  originalFiles.value = JSON.parse(JSON.stringify(activeItem.value.files));
}

async function saveChanges() {
  const form = new FormData();
  form.append('type', activeItem.value.type);
  form.append('text', activeItem.value.content || '');
  form.append('projectId', String(props.projectId));

  try {
    await axios.put('/develop/update', form, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    alert('저장되었습니다.');
  } catch (err) {
    console.error('저장 실패', err);
    alert('저장 중 오류가 발생했습니다.');
    activeItem.value.content = originalContent.value;
  } finally {
    isEditing.value = false;
  }
}

function cancelEditing() {
  if (confirm('수정 중인 내용을 취소하시겠습니까?')) {
    activeItem.value.content = originalContent.value;
    isEditing.value = false;
  }
}

async function loadFeedbacks() {
  if (!activeItem.value) return;
  const pageIdentifier = `develop-${activeItem.value.type}`;
  try {
    const { data } = await axios.get('/feedbacks/project', {
      params: { page: pageIdentifier, projectId: props.projectId },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    feedbacks.value = data.filter(fb => !fb.isRead);
  } catch (err) { console.error('피드백 로딩 실패', err); }
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
      item.content = res.data[item.type]?.text || '';
      item.files   = res.data[item.type]?.files || []
      item.completed =
        Boolean(item.content.trim()) || item.files.length > 0
    })
    emit('updateStepProgress', devItems.filter(i => i.completed).length)
    loadFeedbacks();

    // '기능별 개발 순서' 탭 데이터 로드
    // await fetchDevOrderItems(); // DevOrderTable 컴포넌트에서 자체적으로 로드
  } catch (err) {
    console.error('데이터 로딩 오류', err)
  }
})

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
const formatDate = date => new Date(date).toLocaleString()
</script>

<style scoped>
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.schedule-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 18px;
  border: 1px solid #dee2e6;
  border-radius: 8px;
  background: #fff;
  color: #495057;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s;
}
.schedule-btn:hover {
  background: #f8f9fa;
  border-color: #adb5bd;
}
.planning-details { display: flex; flex-direction: column; gap: 24px; position: relative; }
.nav-buttons { display: flex; gap: 8px; flex-wrap: wrap; }
.nav-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 18px;
  border: none;
  border-radius: 8px;
  background: #f1f3f5;
  color: #495057;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all .2s;
}
.nav-btn:hover { background: #e9ecef; }
.nav-btn.active { background: #3f8efc; color: #fff; }
.nav-btn .check-icon { transition: all .2s; }
.nav-btn.completed:not(.active) { color: #3f8efc; }
.nav-btn.completed:not(.active) .check-icon { color: #3f8efc; }
.nav-btn.active .check-icon { color: #fff; }
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

.content-grid { display: flex; flex-direction: column; gap: 24px; }
.editor-wrapper { min-width: 0; }
.editor-container {
  position: relative;
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
  display: flex;
  flex-direction: column;
  height: 100%;
}
.readonly-content {
  flex-grow: 1;
  line-height: 1.7;
  color: #343a40;
  white-space: pre-wrap;
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  min-height: 400px;
}
.readonly-content.empty { color: #adb5bd; font-style: italic; }

.upload-section { display: flex; flex-direction: column; gap: 12px; }
.section-title { font-size: 18px; font-weight: 700; color: #343a40; margin: 0; }
.attachments-container {
  background: #fff;
  border: 1px solid #e9ecef;
  width: 100%;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.04);
}
.upload-zone {
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #d0dffc;
  border-radius: 10px;
  text-align: center;
  color: #3f8efc;
  cursor: pointer;
  transition: background-color .2s;
  aspect-ratio: 1 / 1;
}
.upload-zone:hover { background: #f3f8ff; }
.upload-message { display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; font-weight: 600; }
.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 120px);
  gap: 12px;
}
.file-card {
  position: relative;
  border-radius: 10px;
  overflow: hidden;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.07);
  transition: all .2s;
}
.file-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0,0,0,0.1);
}
.file-delete-btn {
  position: absolute; top: -8px; right: -8px;
  background: #343a40;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 24px; height: 24px;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  opacity: 0;
  transform: scale(0.8);
  transition: all .2s;
  z-index: 2;
}
.file-card:hover .file-delete-btn { opacity: 1; transform: scale(1); }
.file-preview {
  width: 100%;
  aspect-ratio: 1 / 1;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.file-thumb { width: 100%; height: 100%; object-fit: cover; }
.file-icon { color: #ced4da; }
.file-info { padding: 8px; background: #fff; border-top: 1px solid #f1f3f5; }
.file-name {
  display: block; font-size: 12px; font-weight: 600; color: #343a40;
  white-space: nowrap; overflow: hidden; text-overflow: ellipsis; margin-bottom: 4px;
}
.file-date { font-size: 10px; color: #868e96; display: block; }
.empty-files { text-align: center; color: #adb5bd; padding: 24px; font-size: 14px; width: 100%; }

.basic-textarea {
  width: 100%;
  min-height: 100px;
  padding: 20px;
  font-size: 16px;
  line-height: 1.7;
  border: none;
  border-radius: 8px;
  resize: none;
  white-space: pre-wrap;
  background-color: #fff;
  color: #343a40;
  transition: box-shadow 0.2s;
}
.basic-textarea:focus { outline: none; }
.basic-textarea.large { min-height: 400px; flex-grow: 1; }
.basic-textarea:read-only { background-color: #fff; box-shadow: none; }

.edit-controls {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  position: absolute;
  top: 16px;
  right: 24px;
}
.control-btn {
  padding: 6px 14px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}
.edit-btn { background-color: #f8f9fa; color: #495057; border: 1px solid #dee2e6; }
.edit-btn:hover { background-color: #f1f3f5; }
.save-btn {
  background-color: #3f8efc;
  color: white;
}
.save-btn:hover { background-color: #3578e5; }
.cancel-btn {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
}
.cancel-btn:hover { background-color: #f1f3f5; }
.char-counter {
  text-align: right;
  font-size: 13px;
  color: #868e96;
  margin-top: 8px;
  padding-right: 4px;
}
.feedback-marker {
  font-size: 20px;
  cursor: pointer;
}
</style>
