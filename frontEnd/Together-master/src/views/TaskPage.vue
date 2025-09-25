<template>
  <div class="task-board-page" @contextmenu.prevent="handleRightClick" style="position: relative;">
    <!-- 검색 + 필터 -->
    <div class="search-bar">
      <div class="search-filters">
        <input v-model="searchTerm" @input="onSearch" type="text" placeholder="작업 검색..." />
        <div class="filter-toggle">
          <button :class="{ active: filterMode === 'all' }" @click="setFilter('all')">모든 작업</button>
          <button :class="{ active: filterMode === 'mine' }" @click="setFilter('mine')">내 작업</button>
        </div>
        <div class="category-filter-wrapper">
          <select v-model="selectedCategory" @change="onSearch" class="category-filter">
            <option v-for="(label, key) in categories" :key="key" :value="key">{{ label }}</option>
          </select>
        </div>
      </div>

      <!-- 프로젝트 진행률 바 -->
      <div class="progress-container">
        <span class="progress-title">작업 진행률</span>
        <div class="progress-bar-wrapper">
          <div class="progress-bar">
            <div class="progress-fill" :style="{ width: progress + '%' }"></div>
          </div>
          <span class="progress-percentage">{{ progress }}%</span>
        </div>
      </div>
    </div>

    <div class="board">
      <!-- 작업 컬럼들 -->
      <div class="column" v-for="(tasks, status) in taskColumns" :key="status">
        <div class="column-header">
          <h3>
            <span>{{ columnTitles[status] }}</span>
            <span class="task-count">{{ tasks.length }}</span>
          </h3>
          <button v-if="status === 'PENDING' && !isReadOnly" @click="openNewTaskModal" class="add-task-btn" title="새 작업 추가">+</button>
        </div>
        <draggable
          v-model="taskColumns[status]"
          itemKey="id"
          :group="{ name: 'tasks' }"
          :disabled="isReadOnly"
          @change="evt => onTaskDrop(evt, status)"
          class="task-list"
        >
          <template #item="{ element: task }">
            <div class="card" @dblclick="openLightbox(task.id)" :style="{ borderLeftColor: getAssigneeDetails(task.assignedUserName)?.userColor || '#e0e0e0' }">
              <div class="card-content">
                <div class="card-title">{{ task.title }}</div>
                <div v-if="task.endDate" class="card-due-date">
                  📅 {{ new Date(task.endDate).toLocaleDateString() }}
                </div>
              </div>
              <div class="card-footer">
                <div class="assignee-info">
                  <span class="card-assignee">{{ task.assignedUserName || '미지정' }}</span>
                  <img :src="getAssigneeDetails(task.assignedUserName)?.profileImageUrl || defaultAvatar" class="assignee-avatar" :alt="task.assignedUserName" />
                </div>
                <span v-if="task.category" class="category-badge" :class="`category-${task.category}`">{{ categories[task.category] }}</span>
              </div>
            </div>
          </template>
        </draggable>
      </div>

      <!-- 📌 피드백 마커 -->
      <div
        v-for="(fb, index) in feedbacks"
        :key="index"
        class="feedback-marker"
        :style="{ top: fb.y + 'px', left: fb.x + 'px', position: 'absolute', zIndex: 10 }"
        @click="selectedFeedback = fb"
      >
        📌
      </div>

      <!-- 피드백 팝업 -->
      <div style="position: absolute; z-index: 20">
        <FeedbackPopup
          v-if="selectedFeedback"
          :fb="selectedFeedback"
          :readonly="true"
          @read="handleReadFeedback"
          @close="selectedFeedback = null"
        />
      </div>

      <!-- 피드백 입력창 (교수 전용) -->
      <FeedbackInput
        v-if="showFeedbackInput"
        :x="feedbackPosition.x"
        :y="feedbackPosition.y"
        :page="'task-board'"
        :readonly="true"
        :projectId="Number(route.params.projectId)"
        @close="showFeedbackInput = false"
        @submitted="() => { showFeedbackInput = false; loadFeedbacks() }"
      />
    </div>

    <div ref="ganttHidden" style="width:0;height:0;overflow:hidden;"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, onActivated, onDeactivated } from 'vue'
import { useRoute } from 'vue-router'
import axios from 'axios'
import draggable from 'vuedraggable'
import gantt from 'dhtmlx-gantt'
import 'dhtmlx-gantt/codebase/dhtmlxgantt.css'
import FeedbackInput from '@/components/feedback/FeedbackInput.vue'
import FeedbackPopup from '@/components/feedback/FeedbackPopup.vue'
import { useFeedback } from '@/composables/useFeedback'

const route = useRoute()
const isReadOnly = computed(() => route.query.readonly === 'true')
const ganttHidden = ref(null)

const searchTerm = ref('')
const selectedCategory = ref('ALL');
const filterMode = ref('all')
const currentUser = ref('')
const workTasks = ref([])
const rawTeamMembers = ref([])
const teamMembers = ref([])
const defaultAvatar = new URL('@/assets/defaultimage.png', import.meta.url).href
const categories = {
  ALL: '전체',
  PLANNING: '기획',
  DESIGN: '설계',
  DEVELOPMENT: '개발',
  TEST: '테스트',
};
const columnTitles = { PENDING: '작업 목록', IN_PROGRESS: '진행 중', COMPLETED: '완료' }
const taskColumns = ref({ PENDING: [], IN_PROGRESS: [], COMPLETED: [] })

const feedbacks = ref([])
const showFeedbackInput = ref(false)
const feedbackPosition = ref({ x: 0, y: 0 })
const selectedFeedback = ref(null)
const { markFeedbackAsRead } = useFeedback()

const ganttEventIds = [] // 이벤트 핸들러 ID 저장용 배열

const progress = computed(() => {
  const total = workTasks.value.length;
  if (total === 0) return 0;
  const completed = workTasks.value.filter(t => t.status === 'COMPLETED').length;
  return Math.round((completed / total) * 100);
});

function handleRightClick(e) {
  if (!isReadOnly.value) return
  const sectionRect = e.currentTarget.getBoundingClientRect()
  feedbackPosition.value = {
    x: e.clientX - sectionRect.left + e.currentTarget.scrollLeft,
    y: e.clientY - sectionRect.top + e.currentTarget.scrollTop
  }
  showFeedbackInput.value = true
}

async function loadFeedbacks() {
  try {
    const res = await axios.get('/feedbacks/project', {
      params: {
        page: 'task-board',
        projectId: route.params.projectId
      },
      headers: { Authorization: localStorage.getItem('authHeader') },
      withCredentials: true
    })
    feedbacks.value = res.data.filter(fb => !fb.isRead)
  } catch (err) {
    console.error('❌ 피드백 불러오기 오류:', err)
  }
}

function handleReadFeedback(id) {
  markFeedbackAsRead(id)
  feedbacks.value = feedbacks.value.filter(fb => fb.feedbackId !== id)
  selectedFeedback.value = null
}

async function fetchCurrentUser() {
  if (isReadOnly.value) return
  try {
    const { data } = await axios.get('/auth/me')
    currentUser.value = data.userName?.trim()
  } catch (e) {
    console.error('사용자 정보 조회 실패:', e)
  }
}

async function fetchTeamMembers() {
  try {
    const { data } = await axios.get('/projects/members', {
      params: { projectId: route.params.projectId }
    })
    rawTeamMembers.value = data
    teamMembers.value = data.map(u => ({
      key: u.userName,
      label: u.userName,
      userId: u.userId,
      userColor: u.userColor,
      profileImageUrl: u.profileImageUrl
    }))
  } catch (e) {
    console.error('팀원 정보 조회 실패:', e)
  }
}

async function fetchTasks() {
  try {
    const { data } = await axios.get('/work-tasks/project', {
      params: { projectId: route.params.projectId }
    })
    workTasks.value = data
    onSearch()
  } catch (e) {
    console.error('작업 목록 조회 실패:', e)
  }
}

function getAssigneeDetails(userName) {
  return rawTeamMembers.value.find(member => member.userName === userName);
}

function splitByStatus(filteredTasks) {
  taskColumns.value.PENDING = filteredTasks.filter(t => t.status === 'PENDING')
  taskColumns.value.IN_PROGRESS = filteredTasks.filter(t => t.status === 'IN_PROGRESS')
  taskColumns.value.COMPLETED = filteredTasks.filter(t => t.status === 'COMPLETED')
}

function onSearch() {
  const q = searchTerm.value.trim().toLowerCase()
  let filtered = workTasks.value
  if (q) filtered = filtered.filter(t => t.title.toLowerCase().includes(q))
  if (filterMode.value === 'mine') {
    filtered = filtered.filter(t => t.assignedUserName?.trim() === currentUser.value.trim())
  }
  if (selectedCategory.value !== 'ALL') {
    filtered = filtered.filter(t => t.category === selectedCategory.value);
  }
  splitByStatus(filtered)
}

function setFilter(mode) {
  filterMode.value = mode
  onSearch()
}

function flattenTask(t, parent = null) {
  const start = new Date(t.startDate)
  const end = new Date(t.endDate)
  const dur = Math.ceil((end - start) / (1000 * 60 * 60 * 24))
  const row = [{ id: t.id, text: t.title, start_date: t.startDate, duration: dur, assignee: t.assignedUserName, parent, category: t.category }]
  if (t.childTasks) t.childTasks.forEach(c => row.push(...flattenTask(c, t.id)))
  return row
}

function refreshHiddenGantt() {
  const rows = workTasks.value.flatMap(t => flattenTask(t))
  gantt.clearAll()
  gantt.parse({ data: rows, links: [] })
}

async function onTaskDrop(evt, newStatus) {
  if (isReadOnly.value || !evt.added) return
  const task = evt.added.element
  const payload = {
    title: task.title,
    description: task.description || '',
    startDate: task.startDate,
    endDate: task.endDate,
    assignedUserId: task.assignedUserId,
    status: newStatus,
    parentTaskId: task.parentTaskId,
    category: task.category
  }
  try {
    await axios.patch(`/work-tasks/${task.id}`, payload)
  } catch (e) {
    console.error('작업 상태 변경 실패:', e)
  } finally {
    await fetchTasks()
    refreshHiddenGantt()
  }
}

function openLightbox(id) {
  if (isReadOnly.value) return
  refreshHiddenGantt()
  gantt.showLightbox(id)
}

function openNewTaskModal() {
  if (isReadOnly.value) return;
  refreshHiddenGantt();
  gantt.createTask({ status: 'PENDING' }); // 새 작업 생성 시 기본 상태를 'PENDING'으로 설정
}

async function loadAndInitialize() {
  // Gantt 초기화
  if (ganttHidden.value) {
    gantt.init(ganttHidden.value);
  }

  gantt.config.readonly = isReadOnly.value
  gantt.locale.labels.section_description = '작업 이름'
  gantt.locale.labels.section_time = '기간'
  gantt.locale.labels.section_assignee = '담당자'
  gantt.locale.labels.section_category = '분류'
  gantt.locale.labels.button_save = '저장'
  gantt.locale.labels.button_cancel = '취소'
  gantt.locale.labels.button_delete = '삭제'

  gantt.config.lightbox.sections = [
    { name:'description', map_to:'text', type:'textarea', label:'작업 이름' },
    { name:'category', map_to:'category', type:'select', options: [
        { key: 'PLANNING', label: '기획' },
        { key: 'DESIGN', label: '설계' },
        { key: 'DEVELOPMENT', label: '개발' },
        { key: 'TEST', label: '테스트' }
    ], label:'카테고리' },
    { name:'time', map_to:'auto', type:'duration', label:'기간' },
    { name:'assignee', map_to:'assignee', type:'select', options: teamMembers.value, label:'담당자' }
  ]

  // 이전 이벤트 핸들러 정리
  ganttEventIds.forEach(id => gantt.detachEvent(id));
  ganttEventIds.length = 0;

  if (!isReadOnly.value) {
    ganttEventIds.push(gantt.attachEvent("onAfterTaskAdd", async (tempId, task) => {
      const startStr = gantt.date.date_to_str("%Y-%m-%d")(task.start_date)
      const endStr = gantt.date.date_to_str("%Y-%m-%d")(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration }))
      const sel = rawTeamMembers.value.find(u => u.userName === task.assignee)
      const payload = { title: task.text, description: task.text, startDate: startStr, endDate: endStr, assignedUserId: sel?.userId ?? null, status: 'PENDING', parentTaskId: task.parent || null, category: task.category }
      try {
        const { data } = await axios.post('/work-tasks', payload)
        gantt.changeTaskId(tempId, data.id)
      } catch (err) {
        console.error('작업 생성 실패', err);
        gantt.deleteTask(tempId)
      } finally {
        await fetchTasks()
        refreshHiddenGantt()
      }
    }));

    ganttEventIds.push(gantt.attachEvent('onAfterTaskUpdate', async (id, task) => {
      const startStr = gantt.date.date_to_str('%Y-%m-%d')(task.start_date)
      const endStr = gantt.date.date_to_str('%Y-%m-%d')(gantt.calculateEndDate({ start_date: task.start_date, duration: task.duration }))
      const sel = rawTeamMembers.value.find(u => u.userName === task.assignee)
      const dto = {
        title: task.text,
        description: task.text,
        startDate: startStr,
        endDate: endStr,
        assignedUserId: sel?.userId ?? null,
        status: task.status,
        parentTaskId: task.parent || null,
        category: task.category
      }
      try {
        await axios.patch(`/work-tasks/${id}`, dto)
      } catch (e) {
        console.error('작업 업데이트 실패:', e)
      } finally {
        await fetchTasks()
        refreshHiddenGantt()
      }
    }));

    ganttEventIds.push(gantt.attachEvent('onBeforeTaskDelete', async id => {
      try {
        await axios.delete(`/work-tasks/${id}`)
      } catch (e) {
        console.error('작업 삭제 실패:', e)
      } finally {
        await fetchTasks()
        refreshHiddenGantt()
      }
      return true
    }));
  }

  refreshHiddenGantt()
}

function cleanup() {
  // Gantt 인스턴스를 파괴하는 대신, 등록된 이벤트만 정리합니다.
  // destructor()는 다른 페이지에서 사용하는 gantt 객체까지 파괴하여 오류를 유발합니다.
  ganttEventIds.forEach(id => gantt.detachEvent(id));
  ganttEventIds.length = 0;
  // 다른 페이지에서의 재초기화를 위해 차트 내용을 비웁니다.
  gantt.clearAll();
}

onMounted(async () => {
  await fetchCurrentUser();
  await fetchTeamMembers();
  await fetchTasks();
  await loadFeedbacks();
  loadAndInitialize();
});

onBeforeUnmount(cleanup);
onActivated(async () => {
  // <keep-alive>로 다시 활성화될 때 데이터와 Gantt를 다시 로드
  await fetchCurrentUser();
  await fetchTeamMembers();
  await fetchTasks();
  await loadFeedbacks();
  loadAndInitialize();
});
onDeactivated(cleanup);

</script>


<style scoped>
.task-board-page {
  padding: 24px;
  background-color: #f7f8fc;
  height: 100vh;
  box-sizing: border-box;
}

.search-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
  padding: 8px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  max-width: 1200px;
  margin-left: auto;
  margin-right: auto;
}

.search-filters {
  display: flex;
  align-items: center;
}

.category-filter-wrapper {
  margin-left: 12px;
}


.search-bar input {
  width: 220px;
  padding: 8px 12px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  font-size: 14px;
}
.search-bar input:focus {
  border-color: #80bdff;
  outline: 0;
  box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
}

.filter-toggle {
  margin-left: 12px;
}

.filter-toggle button {
  margin-left: 5px;
  padding: 8px 14px;
  border: 1px solid #dee2e6;
  border-radius: 6px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.filter-toggle button.active {
  background: #3f8efc;
  color: #fff;
  border-color: #3f8efc;
}

.category-filter {
  padding: 8px 12px;
  border-radius: 6px;
  border: 1px solid #dee2e6;
  font-size: 14px;
}

.progress-container {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 250px;
}

.progress-title {
  font-size: 14px;
  font-weight: 600;
  color: #495057;
  white-space: nowrap;
}

.progress-bar-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.progress-bar {
  flex-grow: 1;
  height: 10px;
  background-color: #e9ecef;
  border-radius: 5px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #3f8efc;
  border-radius: 5px;
  transition: width 0.5s ease-in-out;
}

.divider {
  display: none;
}

.board {
  display: flex;
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
  height: calc(100vh - 150px);
}

.column {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: #f1f3f5;
  border-radius: 12px;
  padding: 16px;
  min-width: 200px;
}
.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 8px;
  margin-bottom: 12px;
}
.column h3 {
  text-align: center;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}
.column h3 span:first-child {
  background: #4A90E2;
  color: white;
  border-radius: 999px;
  padding: 4px 15px;
  font-size: 1rem;
}
.task-count {
  font-size: 0.9rem;
  color: #666;
  background-color: #e0e0e0;
  padding: 2px 8px;
  border-radius: 10px;
}
.add-task-btn {
  background: #767676;
  color: white;
  border: none;
  border-radius: 50%;
  width: 28px;
  height: 28px;
  font-size: 20px;
  line-height: 28px;
  cursor: pointer;
  transition: background-color 0.2s;
}
.add-task-btn:hover {
  background: #5a5a5a;
}
.task-list {
  flex-grow: 1;
  min-height: 100px;
  padding: 4px;
  overflow-y: auto;
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.task-list::-webkit-scrollbar {
  display: none;
}

.card {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  background: #fff;
  border-radius: 6px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  cursor: grab;
  border-left: 5px solid #e0e0e0;
  transition: box-shadow 0.2s;
  min-height: 80px;
}
.card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}
.card-content {
  margin-bottom: 12px;
}
.category-badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 11px;
  font-weight: 600;
  border-radius: 10px;
  color: white;
}
.category-PLANNING {
  background-color: #ffaeae;
}
.category-DESIGN {
  background-color: #f39c12;
}
.category-DEVELOPMENT {
  background-color: #2ecc71;
}
.category-TEST {
  background-color: #9b59b6;
}
.card-title {
  font-size: 1rem;
  margin-bottom: 4px;
  font-weight: 500;
  color: #343a40;
}
.card-due-date {
  font-size: 0.8rem;
  color: #888;
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.assignee-info {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-assignee {
  font-size: 0.85rem;
  color: #6c757d;
}
.assignee-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.feedback-marker {
  font-size: 18px;
  cursor: pointer;
  position: absolute;
}

.popup-container {
  position: absolute;
  z-index: 9999; /* 마커보다 위로 */
  background: white;
  padding: 12px;
  border: 1px solid #ccc;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}
</style>
