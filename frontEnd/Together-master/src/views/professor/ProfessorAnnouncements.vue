<template>
  <div class="announcements-container">
    <header class="page-header">
      <h1>📢 공지사항 관리</h1>
      <p>프로젝트를 선택하여 공지사항을 확인하고 작성할 수 있습니다.</p>
    </header>

    <div class="controls-wrapper card">
      <div class="project-selector">
        <label for="project-select">프로젝트 선택:</label>
        <select id="project-select" v-model="selectedProjectId" @change="fetchNotices">
          <option :value="null" disabled>-- 프로젝트를 선택하세요 --</option>
          <option v-for="project in projects" :key="project.projectId" :value="project.projectId">
            {{ project.title }}
          </option>
        </select>
      </div>
      <button class="create-btn" @click="openCreateModal" :disabled="!selectedProjectId">
        새 공지 작성
      </button>
    </div>

    <div class="content-wrapper card">
      <div v-if="!selectedProjectId" class="empty-state">
        <p>먼저 프로젝트를 선택해주세요.</p>
      </div>
      <div v-else-if="isLoading" class="loading-state">
        <p>공지사항을 불러오는 중입니다...</p>
      </div>
      <div v-else>
        <NoticeList :notices="notices" @selectNotice="openNoticeDetail" />
        <div v-if="notices.length === 0" class="empty-state">
          <p>이 프로젝트에는 공지사항이 없습니다.</p>
        </div>
      </div>
    </div>

    <!-- 공지 생성 모달 -->
    <NoticeCreateModal
      v-if="showCreateModal"
      :writerName="currentUserName"
      @create="handleCreateNotice"
      @close="showCreateModal = false"
    />

    <!-- 공지 상세 모달 -->
    <NoticeDetailModal
      v-if="showDetailModal"
      :notice="selectedNotice"
      @close="showDetailModal = false"
      @update="handleUpdateNotice"
      @delete="handleDeleteNotice"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from '@/utils/axiosInstance.js';
import NoticeList from '@/components/notice/NoticeList.vue';
import NoticeDetailModal from '@/components/notice/NoticeDetailModal.vue';
import NoticeCreateModal from '@/components/notice/NoticeCreateModal.vue';

const projects = ref([]);
const selectedProjectId = ref(null);
const notices = ref([]);
const isLoading = ref(false);
const currentUserName = ref('');

const showCreateModal = ref(false);
const showDetailModal = ref(false);
const selectedNotice = ref(null);

async function fetchProfessorProjects() {
  try {
    // ProfessorMainPage.vue에서 사용하는 것과 동일한 방식으로 프로젝트 목록을 가져옵니다.
    const { data: meData } = await axios.get('/auth/me');
    const projectList = meData.projectId || [];

    // TeamCard와 달리 여기서는 title과 projectId만 필요하므로,
    // projectList에 이미 해당 정보가 있다면 그대로 사용합니다.
    // 만약 상세 정보가 필요하다면, ProfessorMainPage.vue처럼 Promise.all을 사용해야 합니다.
    projects.value = projectList;

  } catch (error) {
    console.error('교수 프로젝트 목록 로딩 실패:', error);
    alert('관리 중인 프로젝트 목록을 불러오는 데 실패했습니다.');
  }
}

async function fetchNotices() {
  if (!selectedProjectId.value) {
    notices.value = [];
    return;
  }
  isLoading.value = true;
  try {
    const { data } = await axios.get(`/notices/all-notice?projectId=${selectedProjectId.value}`);
    notices.value = data
      .sort((a, b) => new Date(b.createdDate) - new Date(a.createdDate))
      .map(n => ({
        ...n,
        writerName: n.writerName || n.authorName || n.userName || currentUserName.value
      }));
  } catch (error) {
    console.error('공지사항 로딩 실패:', error);
    notices.value = [];
  } finally {
    isLoading.value = false;
  }
}

async function fetchMe() {
  try {
    const { data } = await axios.get('/auth/me');
    currentUserName.value = data.userName;
  } catch (error) {
    console.error('사용자 정보 로딩 실패:', error);
  }
}

function openCreateModal() {
  if (!selectedProjectId.value) {
    alert('먼저 프로젝트를 선택해주세요.');
    return;
  }
  showCreateModal.value = true;
}

function openNoticeDetail(notice) {
  selectedNotice.value = notice;
  showDetailModal.value = true;
}

async function handleCreateNotice(newNoticeData) {
  try {
    await axios.post(
      `/notices/create?projectId=${selectedProjectId.value}`,
      newNoticeData
    );
    showCreateModal.value = false;
    await fetchNotices();
  } catch (e) {
    console.error('공지사항 생성 실패:', e);
    alert('공지사항 생성에 실패했습니다.');
  }
}

async function handleUpdateNotice(updatedData) {
  try {
    await axios.put(`/notices/update/${updatedData.noticeId}`, updatedData);
    showDetailModal.value = false;
    await fetchNotices();
  } catch (e) {
    console.error('공지사항 수정 실패:', e);
    alert('공지사항 수정에 실패했습니다.');
  }
}

async function handleDeleteNotice(noticeId) {
  try {
    await axios.delete(`/notices/delete/${noticeId}`);
    showDetailModal.value = false;
    await fetchNotices();
  } catch (e) {
    console.error('공지사항 삭제 실패:', e);
    alert('공지사항 삭제에 실패했습니다.');
  }
}

onMounted(() => {
  fetchMe();
  fetchProfessorProjects();
});
</script>

<style scoped>
.announcements-container {
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 24px;
  background-color: #f7f8fc;
  height: calc(100vh - 60px); /* 헤더 높이 제외 */
}

.page-header {
  padding-bottom: 16px;
  border-bottom: 1px solid #e9ecef;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
}

.page-header p {
  font-size: 16px;
  color: #6c757d;
}

.card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
}

.controls-wrapper {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-selector {
  display: flex;
  align-items: center;
  gap: 12px;
}

.project-selector label {
  font-weight: 600;
}

.project-selector select {
  padding: 8px 12px;
  border-radius: 8px;
  border: 1px solid #ced4da;
  min-width: 250px;
}

.create-btn {
  padding: 10px 20px;
  background-color: #3f8efc;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  transition: background-color 0.2s;
}

.create-btn:hover {
  background-color: #3578e5;
}

.create-btn:disabled {
  background-color: #adb5bd;
  cursor: not-allowed;
}

.content-wrapper {
  flex-grow: 1;
  overflow-y: auto;
}

.empty-state, .loading-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #868e96;
  font-size: 16px;
}
</style>