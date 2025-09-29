<template>
  <section class="planning-details">
    <div class="section-header">
      
      <!-- 상단 버튼 네비게이션 -->
      <div class="nav-buttons">
        <button 
          v-for="(item, idx) in planningItems"
          :key="idx"
          :class="['nav-btn', { active: selectedIndex === idx, completed: item.completed }]"
          @click="selectTab(idx)"
        >
          <svg v-if="item.completed" class="check-icon" xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"></polyline></svg>
          <span>{{ item.name }}</span>
          <svg v-if="item.type === 'infostructure'" class="link-icon" xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><polyline points="15 3 21 3 21 9"></polyline><line x1="10" y1="14" x2="21" y2="3"></line></svg>
        </button>
      </div>
      <button @click="openScheduleModal" class="schedule-btn">
        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
        기획 일정 보기
      </button>
    </div>

    <div class="content-grid">
      <!-- 왼쪽: 편집 영역 -->
      <div class="editor-wrapper">
        <div class="editor-container" @contextmenu.prevent="handleRightClick">
          <!-- 수정/저장/취소 버튼 -->
          <div class="edit-controls" v-if="!props.readonly">
            <button v-if="!isEditing" @click="startEditing" class="control-btn edit-btn">수정</button>
            <template v-else>
              <button @click="saveChanges" class="control-btn save-btn">저장</button>
              <button @click="cancelEditing" class="control-btn cancel-btn">취소</button>
            </template>
          </div>

          <!-- 내용 표시 영역 -->
          <template v-if="activeItem.type === 'storyboard'">
            <textarea
              v-model="activeItem.content"
              class="basic-textarea"
              :readonly="!isEditing || readonly"
              placeholder="스토리보드 또는 Figma 공유 링크 입력"
              @input="onContentChange(($event.target as HTMLTextAreaElement)?.value || '')"
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
            <textarea
              v-if="isEditing && !readonly"
              v-model="activeItem.content"
              class="basic-textarea large"
              placeholder="내용을 입력하세요..."
              @input="onContentChange((($event.target as HTMLTextAreaElement)?.value) || '')"
            ></textarea>
            <div v-else class="readonly-content" :class="{ empty: !activeItem.content }">{{ activeItem.content || '내용이 없습니다.' }}</div>
          </template>
          <div v-if="isEditing" class="char-counter">
            {{ (activeItem.content || '').length }} / 2000
          </div>
        </div>
      </div>

      <!-- 오른쪽: 파일 및 이미지 업로드 -->
      <div class="upload-section">
        <h4 class="section-title">첨부 파일</h4>
        <div class="attachments-container">
          <div class="file-grid">
            <!-- 파일 업로드 카드 (수정 모드가 아니어도 보이도록 수정) -->
            <div v-if="!props.readonly" class="upload-zone" @click="fileInputRef?.click()"
                @dragover.prevent
                @drop.prevent="handleDrop">
              <input type="file" multiple ref="fileInputRef" @change="onFileSelect" hidden />
              <div class="upload-message">
                <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path><polyline points="17 8 12 3 7 8"></polyline><line x1="12" y1="3" x2="12" y2="15"></line></svg>
                <span>파일 업로드</span>
              </div>
            </div>
            
            <!-- 첨부된 파일 카드 -->
            <div v-for="(file, i) in activeItem.files" :key="file.url || i" class="file-card" :class="{ uploading: file.isUploading }">
              <button v-if="!props.readonly" class="file-delete-btn" @click.stop="removeFile(i)">
                <svg xmlns="http://www.w3.org/2000/svg" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"><line x1="18" y1="6" x2="6" y2="18"></line><line x1="6" y1="6" x2="18" y2="18"></line></svg>
              </button>
              <div class="file-preview">
                <img v-if="isImage(file)" :src="toDrivePreview(file.url)" class="file-thumb" />
                <div v-else class="file-icon">
                  <svg xmlns="http://www.w3.org/2000/svg" width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
                </div>
                <div v-if="file.isUploading" class="upload-indicator">
                  <div class="spinner"></div>
                </div>
              </div>
            <div class="file-info">
              <a class="file-name" :href="file.url" target="_blank" :title="extractFileName(file.url)">{{ extractFileName(file.url) }}</a>
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

    <!-- 피드백 마커 -->
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
      :readonly="true"
      :projectId="resolvedProjectId"
      :page="`planning-${activeItem.type}`"
      @close="showFeedbackInput = false"
      @submitted="() => { showFeedbackInput = false; loadFeedbacks(`planning-${activeItem.type}`) }"
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

    <!-- 카테고리별 일정 모달 -->
    <CategoryScheduleModal
      v-if="showScheduleModal"
      :project-id="resolvedProjectId"
      category="PLANNING"
      :readonly="props.readonly"
      @close="showScheduleModal = false"
    />
  </section>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import ContextMenu from '@/components/feedback/ContextMenu.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import CategoryScheduleModal from './CategoryScheduleModal.vue'
import FeedbackNote from '@/components/feedback/FeedbackNote.vue'
import { useFeedback } from '@/composables/useFeedback.js'
import { useRouter, useRoute } from 'vue-router'

interface FileObject {
  tempId?: string;
  url: string;
  isUploading?: boolean;
  uploadedAt: string;
  originalFile?: File;
  contentType?: string;
}

interface PlanningItem {
  name: string;
  type: string;
  content: string;
  files: FileObject[];
  completed: boolean;
}

interface Feedback {
  feedbackId: number;
  x: number;
  y: number;
  isRead: boolean;
}

const router = useRouter()
const route = useRoute()
const fileInputRef = ref<HTMLInputElement | null>(null)
const props = defineProps({ projectId: Number, readonly: Boolean })
const resolvedProjectId = computed(() => props.projectId || Number(route.params.projectId))
const emit = defineEmits(['updateStepProgress'])

const feedbacks = ref<Feedback[]>([])
const showFeedbackInput = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const showContextMenu = ref(false)
const selectedFeedback = ref<Feedback | null>(null)
const { markFeedbackAsRead } = useFeedback()

const isEditing = ref(false);
const originalContent = ref('');
const originalFiles = ref<FileObject[]>([]);
const showScheduleModal = ref(false);

const openScheduleModal = () => {
  showScheduleModal.value = true;
};

function extractFigmaUrl(content: string | null): string {
  if (!content) return ''
  const match = content.match(/https:\/\/www\.figma\.com\/(file|design)\/[^\s<"]+/)
  return match ? match[0] : ''
}

function convertToFigmaEmbed(link: string) {
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

function isValidFigmaLink(content: string): boolean {
  return extractFigmaUrl(content) !== ''
}

function handleRightClick(e: MouseEvent) {
  if (!props.readonly) return
  const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop

  feedbackPosition.value = {
    x: e.clientX + scrollLeft,
    y: e.clientY + scrollTop
  }
  showContextMenu.value = true
}

function handleMenuSelect(action: string) {
  if (action === 'add-feedback') {
    showFeedbackInput.value = true
  }
}


function handleReadFeedback(id: number) {
  markFeedbackAsRead(id)
  feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== id)
  selectedFeedback.value = null
}

async function loadFeedbacks(pageIdentifier: string) {
  if (!pageIdentifier) return;
  try {
    const { data } = await axios.get('/feedbacks/project', {
      params: { page: pageIdentifier, projectId: resolvedProjectId.value },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    feedbacks.value = data.filter((fb: Feedback) => !fb.isRead)
  } catch (err) {
    console.error('❌ 피드백 불러오기 실패:', err)
  }
}
const planningItems: PlanningItem[] = reactive([
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

function selectTab(idx: number) {
  const type = planningItems[idx].type
  // 만약 정보구조도(또는 다른 다이어그램)라면 페이지 이동
  if (type === 'infostructure') {
    router.push({
      path: `/info-structure/${resolvedProjectId.value}`,
      query: {
        readonly: String(props.readonly ?? route.query.readonly === 'true'),
        projectTitle: route.query.projectTitle || '프로젝트'
      }
    })
  } else {
    selectedIndex.value = idx
    isEditing.value = false; // 탭 전환 시 수정 모드 해제
    loadFeedbacks(`planning-${planningItems[idx].type}`);
  }
}

import { watch } from 'vue'

watch(() => route.query.substep, (newSubstep) => {
  if (newSubstep) {
    const index = planningItems.findIndex(item => item.type === newSubstep);
    if (index !== -1) {
      // selectTab 함수를 호출하여 페이지 이동 로직까지 처리
      selectTab(index);
    }
  }
}, { immediate: true });


function markCompleted() {
  activeItem.value.completed = Boolean(activeItem.value.content.trim()) || activeItem.value.files.length > 0
  emit('updateStepProgress', planningItems.filter(i => i.completed).length)
}
function onContentChange(val: string) {
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
  let contentToSave = activeItem.value.content || '';

  // TinyMCE가 남기는 빈 태그를 제거합니다.
  if (contentToSave.trim() === '<p><br data-mce-bogus="1"></p>' || contentToSave.trim() === '<p>&nbsp;</p>') {
    contentToSave = '';
  }

  form.append('type', activeItem.value.type as string);
  form.append('text', contentToSave);
  form.append('projectId', String(props.projectId));

  try {
    await axios.put('/planning/update', form, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    });
    // 파일 변경 사항은 각 파일 업로드/삭제 시 이미 서버에 반영되었으므로
    // 여기서는 텍스트 내용만 저장합니다.
    alert('저장되었습니다.');
  } catch (err) {
    console.error('저장 실패', err);
    alert('저장 중 오류가 발생했습니다.');
    // 실패 시 원상 복구
    activeItem.value.content = originalContent.value;
    activeItem.value.files = originalFiles.value;
  } finally {
    isEditing.value = false;
  }
}

function cancelEditing() {
  if (confirm('수정 중인 내용을 취소하시겠습니까? 저장되지 않은 변경사항은 사라집니다.')) {
    activeItem.value.content = originalContent.value;
    // 파일 변경사항은 즉시 반영되므로, 취소 시 복구 로직이 복잡해짐.
    // 여기서는 간단하게 텍스트만 복구하고, 사용자에게 파일은 수동으로 관리하도록 안내할 수 있음.
    // 혹은, 파일 변경도 임시 상태로 두었다가 저장 시 한 번에 반영하는 로직으로 변경 필요.
    // 현재는 텍스트만 복구합니다.
    // activeItem.value.files = originalFiles.value; // 이 부분은 파일 시스템 복잡성으로 인해 주석 처리
    isEditing.value = false;
    alert('수정이 취소되었습니다. 파일 변경사항은 이미 반영되었을 수 있습니다.');
  }
}

function onFileSelect(e: Event) {
  const input = e.target as HTMLInputElement | null;
  if (!input || !input.files) return;
  const files = Array.from(input.files);
  uploadFiles(files);
}

function handleDrop(e: DragEvent) {
  if (!e.dataTransfer) return;
  const files = Array.from(e.dataTransfer.files);
  uploadFiles(files);
}

function uploadFiles(files: File[]) {
  const tempFiles = files.map(file => ({
    tempId: `temp-${Date.now()}-${Math.random()}`,
    url: URL.createObjectURL(file),
    isUploading: true,
    uploadedAt: new Date().toISOString(),
    originalFile: file,
  }));

  // 즉시 미리보기를 위해 임시 파일 추가
  activeItem.value.files.push(...tempFiles);

  tempFiles.forEach(tempFile => {
    const form = new FormData();
    form.append('type', activeItem.value.type as string);
    form.append('projectId', String(props.projectId ?? ''));
    form.append('files', tempFile.originalFile);

    axios.post('/planning/upload', form, {
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
      .then(res => {
        const uploadedFile = res.data.files[0];
        const index = activeItem.value.files.findIndex(f => f.tempId === tempFile.tempId);
        if (index !== -1) {
          // 업로드 완료 후, 서버에서 받은 정보로 교체
          activeItem.value.files.splice(index, 1, uploadedFile);
        }
        markCompleted();
      })
      .catch(err => {
        console.error('파일 업로드 오류', err);
        alert(`${tempFile.originalFile.name} 파일 업로드에 실패했습니다.`);
        // 실패 시, 임시 파일 제거
        const index = activeItem.value.files.findIndex(f => f.tempId === tempFile.tempId);
        if (index !== -1) activeItem.value.files.splice(index, 1);
      });
  });
}

async function removeFile(idx: number) {
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

    // 🔥 현재 활성화된 탭의 피드백 불러오기
    if (activeItem.value) {
      await loadFeedbacks(`planning-${activeItem.value.type}`);
    }

  } catch (err) {
    console.error('데이터 로딩 오류', err)
  }
})


const extractFileName = (url: string) => url ? url.split('/').pop() : ''
const isImage = (file: FileObject): boolean => {
  // For local files being uploaded
  if (file.originalFile && file.originalFile.type) {
    return file.originalFile.type.startsWith('image/');
  }
  // For files from backend that have contentType
  if (file.contentType) {
    return file.contentType.startsWith('image/');
  }
  // Fallback for files that only have a URL
  if (file.url) {
    // Check for Google Drive link
    if (file.url.includes('drive.google.com/')) {
      return true;
    }
    // Check for standard image extensions
    return /\.(jpe?g|png|gif|bmp|webp)$/i.test(file.url);
  }
  return false;
}
const toDrivePreview = (url: string): string => {
  if (!url || typeof url !== 'string') return '';

  // Case 1: Standard sharing link (file/d/...) or alternative (open?id=...)
  const fileIdMatch = url.match(/file\/d\/([a-zA-Z0-9_-]+)/) || url.match(/open\?id=([a-zA-Z0-9_-]+)/);
  if (fileIdMatch && fileIdMatch[1]) {
    return `https://drive.google.com/uc?export=view&id=${fileIdMatch[1]}`;
  }
  
  // Case 2: Already a direct viewable link (uc?export=view or uc?id=)
  if (url.includes('/uc?')) {
    if (url.includes('export=download')) {
      return url.replace('export=download', 'export=view');
    }
    return url;
  }

  // Fallback
  return url;
};
const formatDate = (date: string) => new Date(date).toLocaleString()
</script>

<style scoped>
.planning-details {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
}

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
/* 상단 탭 버튼 */
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

/* 메인 컨텐츠 그리드 */
.content-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.editor-wrapper {
  min-width: 0;
}

/* 에디터 컨테이너 */
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
  transition: border-color 0.2s, box-shadow 0.2s;
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
.readonly-content.empty {
  color: #adb5bd;
  font-style: italic;
}
.editor-container:focus-within {
  
  box-shadow: 0 0 0 4px rgba(63, 142, 252, 0.2);
}


/* 파일 업로드 */
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
  aspect-ratio: 1 / 1; /* 정사각형 비율 */
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
.file-card.uploading .file-preview::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.5);
  z-index: 1;
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
  aspect-ratio: 1 / 1; /* 정사각형 비율 */
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}
.upload-indicator {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2;
}
.spinner {
  width: 32px;
  height: 32px;
  border: 4px solid rgba(0, 0, 0, 0.1);
  border-left-color: #3f8efc;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
.file-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.file-icon {
  color: #ced4da;
}
.file-info {
  padding: 8px;
  background: #fff;
  border-top: 1px solid #f1f3f5;
}
.file-name {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #343a40;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}
.file-date { font-size: 10px; color: #868e96; display: block; }
.empty-files {
  text-align: center;
  color: #adb5bd;
  padding: 24px;
  font-size: 14px;
  width: 100%;
}
.char-counter {
  text-align: right;
  font-size: 13px;
  color: #868e96;
  margin-top: 8px;
  padding-right: 4px;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
.nav-btn span {
  flex-grow: 1;
}
.link-icon {
  margin-left: 4px;
  color: #adb5bd;
}

/* 피드백 마커 */
.feedback-marker {
  font-size: 20px;
  cursor: pointer;
}

/* 스토리보드 텍스트 영역 */
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
.basic-textarea:focus {
  outline: none;
}
.basic-textarea.large {
  min-height: 400px; /* 최소 높이 설정 */
  flex-grow: 1; /* 남은 공간을 모두 차지하도록 */
}
.basic-textarea:read-only {
  background-color: #fff;
  box-shadow: none;
}

/* 수정/저장/취소 버튼 */
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
</style>
