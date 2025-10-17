<template>
  <section class="planning-details" @contextmenu.prevent="handleRightClick">
    <div class="section-header">
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

    <template v-if="activeItem.type === 'environment'">
      <div class="card environment-setup-card">
        <div class="card-body">
          <form @submit.prevent="saveEnvironment" class="environment-form">
            <div class="form-row">
              <div class="form-group">
                <label for="os">운영체제 (OS)</label>
                <input id="os" type="text" v-model="environment.operatingSystem" placeholder="예: Windows 11, macOS Sonoma" :disabled="readonly">
              </div>
              <div class="form-group">
                <label for="ide">통합 개발 환경 (IDE)</label>
                <input id="ide" type="text" v-model="environment.ide" placeholder="예: IntelliJ IDEA, VS Code" :disabled="readonly">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label for="lang">개발 언어</label>
                <input id="lang" type="text" v-model="environment.devLanguage" placeholder="예: Java 17, JavaScript ES6" :disabled="readonly">
              </div>
              <div class="form-group">
                <label for="framework">프레임워크</label>
                <input id="framework" type="text" v-model="environment.framework" placeholder="예: Spring Boot 3.1, Vue.js 3" :disabled="readonly">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label for="version-control">버전 관리 시스템</label>
                <input id="version-control" type="text" v-model="environment.versionControl" placeholder="예: Git, SVN" :disabled="readonly">
              </div>
              <div class="form-group">
                <label for="database">데이터베이스</label>
                <input id="database" type="text" v-model="environment.database" placeholder="예: MySQL 8.0, PostgreSQL 15" :disabled="readonly">
              </div>
            </div>
            <div class="form-group full-width">
              <label for="etc">기타</label>
              <textarea id="etc" v-model="environment.etc" placeholder="기타 필요한 라이브러리, 도구 등" :disabled="readonly"></textarea>
            </div>
            <div class="form-actions" v-if="!readonly">
              <button type="submit" class="save-btn">저장</button>
            </div>
          </form>
        </div>
      </div>
    </template>

    <template v-else-if="activeItem.type === 'devOrder'">
      <DevOrderTable :project-id="projectId" />
    </template>

    <template v-else>
      <div class="content-grid">
        <div class="editor-wrapper">
          <div class="editor-container">
            <div class="edit-controls" v-if="!readonly">
              <button v-if="!isEditing" @click="startEditing" class="control-btn edit-btn">수정</button>
              <template v-else>
                <button @click="saveChanges" class="control-btn save-btn">저장</button>
                <button @click="cancelEditing" class="control-btn cancel-btn">취소</button>
              </template>
            </div>

            <textarea v-if="isEditing && !readonly" v-model="activeItem.content" class="basic-textarea large" placeholder="내용을 입력하세요..." @input="onContentChange($event.target.value)"></textarea>
            <div v-else class="readonly-content" :class="{ empty: !activeItem.content }" v-html="activeItem.content || '내용이 없습니다.'"></div>
            <div v-if="isEditing" class="char-counter">{{ (activeItem.content || '').length }} / 2000</div>
          </div>
        </div>

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

    <CategoryScheduleModal
        v-if="showScheduleModal"
        :project-id="props.projectId"
        :readonly="props.readonly"
        category="DEVELOPMENT"
        @close="showScheduleModal = false"
    />

    <ContextMenu
        v-if="showContextMenu"
        :x="feedbackPosition.x"
        :y="feedbackPosition.y"
        :visible="showContextMenu"
        @select="handleMenuSelect"
        @close="showContextMenu = false"
    />

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
import {ref, reactive, computed, onMounted, watch} from 'vue';
import {useRouter, useRoute} from 'vue-router';
import CategoryScheduleModal from './CategoryScheduleModal.vue';
import DevOrderTable from './DevOrderTable.vue';
import api from '@/api';
import ContextMenu from '@/components/feedback/ContextMenu.vue';
import FeedbackInput from '@/components/feedback/FeedbackInput.vue';
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue';
import FeedbackNote from '@/components/feedback/FeedbackNote.vue';
import {useFeedback} from '@/composables/useFeedback.js';

const isEditing = ref(false);
const originalContent = ref('');
const originalFiles = ref([]);

const feedbacks = ref([]);
const showFeedbackInput = ref(false);
const showContextMenu = ref(false);
const feedbackPosition = ref({x: 0, y: 0});
const selectedFeedback = ref(null);
const {markFeedbackAsRead} = useFeedback();

const props = defineProps({projectId: Number, readonly: Boolean, substep: String});
const emit = defineEmits(['updateStepProgress']);

const environment = ref({
  id: null,
  operatingSystem: '',
  ide: '',
  devLanguage: '',
  framework: '',
  versionControl: '',
  database: '',
  etc: ''
});

const fetchEnvironment = async () => {
  if (!props.projectId) return;
  try {
    const response = await api.get('/api/dev-env', {
      headers: {Authorization: localStorage.getItem('authHeader')},
      withCredentials: true,
    });
    // 프로젝트에 설정된 첫 번째 환경 정보를 불러옴
    if (response.data && response.data.length > 0) {
      environment.value = response.data[0];
    }
  } catch (error) {
    console.error('개발 환경 정보를 불러오는 데 실패했습니다.', error);
  }
};

const saveEnvironment = async () => {
  if (!props.projectId) {
    alert('프로젝트 ID가 유효하지 않습니다.');
    return;
  }

  const requestDto = {...environment.value};

  try {
    let response;
    // 기존 환경 정보가 있으면(id가 존재하면) 수정(PUT), 없으면 생성(POST)
    if (environment.value.id) {
      response = await api.put(`/api/dev-env/${environment.value.id}`, requestDto, {
        headers: {Authorization: localStorage.getItem('authHeader')}
      });
    } else {
      response = await api.post('/api/dev-env', requestDto, {
        headers: {Authorization: localStorage.getItem('authHeader')}
      });
      // 새로 생성된 경우, 반환된 id를 받아와 상태에 업데이트
      environment.value.id = response.data;
    }
    alert('개발 환경 정보가 성공적으로 저장되었습니다.');
    await fetchEnvironment(); // 저장 후 최신 정보 다시 불러오기
  } catch (error) {
    console.error('저장에 실패했습니다.', error);
    alert('개발 환경 정보 저장 중 오류가 발생했습니다.');
  }
};

function handleRightClick(e) {
  if (!props.readonly) return;
  const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
  const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
  feedbackPosition.value = {x: e.clientX + scrollLeft, y: e.clientY + scrollTop};
  showContextMenu.value = true;
}

function handleMenuSelect(action) {
  if (action === 'add-feedback') {
    showFeedbackInput.value = true;
  }
}

const fileInputRef = ref(null);


// 타입 이름을 백엔드 엔티티 필드명에 맞춤
const devItems = reactive([
  {name: '개발 환경 설정', type: 'environment', content: '', files: [], completed: false},
  {name: '기능별 개발 순서', type: 'devOrder', content: '', files: [], completed: false, rows: []},
  {name: '커밋 메시지 규칙', type: 'commitRule', content: '', files: [], completed: false},
  {name: '폴더 구조 및 파일 규칙', type: 'folder', content: '', files: [], completed: false}
]);

const router = useRouter();
const route = useRoute();
const selectedIndex = ref(0);
const activeItem = computed(() => devItems[selectedIndex.value]);
const showScheduleModal = ref(false);

const openScheduleModal = () => {
  showScheduleModal.value = true;
};

function selectTab(idx) {
  selectedIndex.value = idx;
  isEditing.value = false;
  if (devItems[idx].type === 'environment') {
    fetchEnvironment();
  }
  loadFeedbacks();
}

watch(() => route.query.substep, (newSubstep) => {
  if (newSubstep) {
    const index = devItems.findIndex(item => item.type === newSubstep);
    if (index !== -1) {
      selectTab(index);
    }
  }
}, {immediate: true});


function markCompleted() {
  activeItem.value.completed =
      Boolean(activeItem.value.content.trim()) ||
      activeItem.value.files.length > 0;
  emit('updateStepProgress',
      devItems.filter(i => i.completed).length
  );
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
    await api.put('/develop/update', form, {
      headers: {Authorization: localStorage.getItem('authHeader')},
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
    const {data} = await api.get('/feedbacks/project', {
      params: {page: pageIdentifier, projectId: props.projectId},
      headers: {Authorization: localStorage.getItem('authHeader')},
      withCredentials: true
    });
    feedbacks.value = data.filter(fb => !fb.isRead);
  } catch (err) {
    console.error('피드백 로딩 실패', err);
  }
}

function onFileSelect(e) {
  uploadFiles(Array.from(e.target.files));
}

function handleDrop(e) {
  uploadFiles(Array.from(e.dataTransfer.files));
}

function uploadFiles(files) {
  const form = new FormData();
  form.append('type', activeItem.value.type);
  form.append('projectId', props.projectId);
  files.forEach(f => form.append('files', f));

  api.post('/develop/upload', form, {
    headers: {Authorization: localStorage.getItem('authHeader')},
    withCredentials: true
  })
      .then(res => {
        activeItem.value.files.push(...res.data.files);
        markCompleted();
      })
      .catch(err => {
        console.error('파일 업로드 오류', err);
        alert('파일 업로드에 실패했습니다.');
      });
}

async function removeFile(idx) {
  const file = activeItem.value.files[idx];
  try {
    await api.delete('/develop/delete-file', {
      params: {
        type: activeItem.value.type,
        fileUrl: file.url
      },
      headers: {Authorization: localStorage.getItem('authHeader')},
      withCredentials: true
    });
    activeItem.value.files.splice(idx, 1);
    markCompleted();
  } catch (err) {
    console.error('파일 삭제 오류', err);
    alert('파일 삭제에 실패했습니다.');
  }
}

onMounted(async () => {
  try {
    const res = await api.get('/develop/all', {
      params: {projectId: props.projectId},
      headers: {Authorization: localStorage.getItem('authHeader')},
      withCredentials: true
    });

    devItems.forEach(item => {
      if (item.type === 'devOrder') return;
      if (item.type === 'environment') {
        fetchEnvironment();
      } else {
        item.content = res.data[item.type]?.text || '';
        item.files = res.data[item.type]?.files || [];
      }
      item.completed =
          Boolean(item.content?.trim()) || (item.files && item.files.length > 0) || (item.type === 'environment' && environment.value.id);
    });
    emit('updateStepProgress', devItems.filter(i => i.completed).length);
    loadFeedbacks();

  } catch (err) {
    console.error('데이터 로딩 오류', err);
  }
});

const extractFileName = url => url.split('/').pop();
const isImage = url =>
    /\.(jpe?g|png|gif|bmp|webp)$/i.test(url) ||
    url.includes('drive.google.com/');
const toDrivePreview = (url) => {
  if (!url) return '';
  const fileIdMatch = url.match(/file\/d\/([a-zA-Z0-9_-]+)/);
  if (fileIdMatch && fileIdMatch[1]) {
    return `https://drive.google.com/uc?export=view&id=${fileIdMatch[1]}`;
  }
  return url.includes('uc?export=download') ? url.replace('export=download', 'export=view') : url;
};
const formatDate = date => new Date(date).toLocaleString();
</script>


<style scoped>
/* DevelopmentEnvironment.vue styles */
.environment-setup-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-body {
  flex-grow: 1;
  overflow-y: auto;
}

.environment-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #343a40;
}

.form-group input,
.form-group textarea {
  padding: 12px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 15px;
  background-color: #f8f9fa;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3f8efc;
  box-shadow: 0 0 0 2px rgba(63, 142, 252, 0.2);
}

.form-group textarea {
  min-height: 100px;
  resize: vertical;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.save-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 8px;
  background-color: #3f8efc;
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.2s;
}

.save-btn:hover {
  background-color: #2a7de9;
}


/* Original DevelopmentDetails.vue styles */
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

.planning-details {
  display: flex;
  flex-direction: column;
  gap: 24px;
  position: relative;
}

.nav-buttons {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.nav-btn {
  display: flex;
  align-items: center;
  gap: 8px;
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

.nav-btn:hover {
  background: #e9ecef;
}

.nav-btn.active {
  background: #3f8efc;
  color: #fff;
}

.nav-btn .check-icon {
  transition: all .2s;
}

.nav-btn.completed:not(.active) {
  color: #3f8efc;
}

.nav-btn.completed:not(.active) .check-icon {
  color: #3f8efc;
}

.nav-btn.active .check-icon {
  color: #fff;
}

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

.content-grid {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.editor-wrapper {
  min-width: 0;
}

.editor-container {
  position: relative;
  background: #fff;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
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

.readonly-content.empty {
  color: #adb5bd;
  font-style: italic;
}

.upload-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: #343a40;
  margin: 0;
}

.attachments-container {
  background: #fff;
  border: 1px solid #e9ecef;
  width: 100%;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
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

.upload-zone:hover {
  background: #f3f8ff;
}

.upload-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-weight: 600;
}

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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.07);
  transition: all .2s;
}

.file-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
}

.file-delete-btn {
  position: absolute;
  top: -8px;
  right: -8px;
  background: #343a40;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transform: scale(0.8);
  transition: all .2s;
  z-index: 2;
}

.file-card:hover .file-delete-btn {
  opacity: 1;
  transform: scale(1);
}

.file-preview {
  width: 100%;
  aspect-ratio: 1 / 1;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
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

.file-date {
  font-size: 10px;
  color: #868e96;
  display: block;
}

.empty-files {
  text-align: center;
  color: #adb5bd;
  padding: 24px;
  font-size: 14px;
  width: 100%;
}

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
  min-height: 400px;
  flex-grow: 1;
}

.basic-textarea:read-only {
  background-color: #fff;
  box-shadow: none;
}

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

.edit-btn {
  background-color: #f8f9fa;
  color: #495057;
  border: 1px solid #dee2e6;
}

.edit-btn:hover {
  background-color: #f1f3f5;
}

.save-btn {
  background-color: #3f8efc;
  color: white;
}

.save-btn:hover {
  background-color: #3578e5;
}

.cancel-btn {
  background-color: #f8f9fa;
  border: 1px solid #dee2e6;
}

.cancel-btn:hover {
  background-color: #f1f3f5;
}

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